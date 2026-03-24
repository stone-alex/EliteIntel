package elite.intel.ai.ears.nemo;

import elite.intel.ai.ears.AudioCalibrator;
import elite.intel.ai.ears.AudioFormatDetector;
import elite.intel.ai.ears.Amplifier;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.ears.Resampler;
import elite.intel.ai.ears.AudioMonitorEvent;
import elite.intel.ai.ears.IsSpeakingEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.STTSanitizer;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.gameapi.AudioMonitorBus.publish;
import static elite.intel.ai.brain.AIConstants.passThroughWords;

public class NemoSTTImpl implements EarsInterface {

    private static final Logger log = LogManager.getLogger(NemoSTTImpl.class);

    private static final String NEMO_WS_URI = "ws://localhost:8000/ws/stt";
    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int ENTER_VOICE_FRAMES = 1;
    private static final int EXIT_SILENCE_FRAMES = 8;
    private static final int PRE_ROLL_FRAMES = 4;
    private static final int MIN_AUDIO_MS = 1500;
    private static final int MIN_AUDIO_BYTES = TARGET_SAMPLE_RATE * 2 * MIN_AUDIO_MS / 1000; // ~48000 bytes

    private final SystemSession systemSession = SystemSession.getInstance();

    private final AtomicBoolean isStopping = new AtomicBoolean(false);
    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);

    private HttpClient httpClient;
    private volatile WebSocket webSocket;
    private Thread processingThread;
    private Resampler resampler;
    private int sampleRateHertz;
    private int bufferSize;
    private double RMS_THRESHOLD_HIGH;
    private double NOISE_FLOOR;

    public NemoSTTImpl() {
        EventBusManager.register(this);
    }

    @Override
    public void start() {
        isStopping.set(false);

        AudioFormatDetector.Format format = AudioFormatDetector.detectSupportedFormat();
        sampleRateHertz = format.getSampleRate();
        bufferSize = format.getBufferSize();

        if (sampleRateHertz != TARGET_SAMPLE_RATE) {
            resampler = new Resampler(sampleRateHertz, TARGET_SAMPLE_RATE, CHANNELS);
        }

        Double high = systemSession.getRmsThresholdHigh();
        Double low = systemSession.getRmsThresholdLow();
        if (high == 0 || low == 0) {
            var cal = AudioCalibrator.calibrateRMS(sampleRateHertz, bufferSize);
            RMS_THRESHOLD_HIGH = cal.getRmsHigh();
            NOISE_FLOOR = cal.getRmsLow();
        } else {
            RMS_THRESHOLD_HIGH = high;
            NOISE_FLOOR = low;
        }

        connectWebSocket();

        isListening.set(true);
        processingThread = new Thread(this::captureLoop, "NeMo-Capture");
        processingThread.setDaemon(true);
        processingThread.start();

        EventBusManager.publish(new AiVoxResponseEvent("NeMo voice input enabled"));
    }

    private void connectWebSocket() {
        httpClient = HttpClient.newHttpClient();
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(NEMO_WS_URI), new NemoListener())
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    log.info("Connected to NeMo STT server at {}", NEMO_WS_URI);
                })
                .exceptionally(ex -> {
                    log.error("Failed to connect to NeMo STT server: {}", ex.getMessage());
                    EventBusManager.publish(new AppLogEvent("NeMo STT: Cannot connect to server at " + NEMO_WS_URI));
                    return null;
                });
    }

    @Override
    public void stop() {
        isStopping.set(true);
        isListening.set(false);
        if (processingThread != null) {
            processingThread.interrupt();
        }
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown")
                    .orTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .exceptionally(ex -> null);
        }
        if (httpClient != null) {
            httpClient.close();
        }
        log.info("NemoSTTImpl stopped");
        EventBusManager.publish(new AiVoxResponseEvent("NeMo voice input disabled"));
    }

    private void captureLoop() {
        AudioFormat audioFormat = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat, bufferSize);
            line.start();

            byte[] buffer = new byte[bufferSize];
            ArrayDeque<byte[]> preRoll = new ArrayDeque<>(PRE_ROLL_FRAMES);
            ByteArrayOutputStream audioCollector = new ByteArrayOutputStream();
            boolean isActive = false;
            int consecutiveVoice = 0;
            int consecutiveSilence = 0;

            while (isListening.get() && !isStopping.get() && !Thread.currentThread().isInterrupted()) {
                int bytesRead = line.read(buffer, 0, bufferSize);
                if (bytesRead <= 0) continue;

                byte[] frame = new byte[bytesRead];
                System.arraycopy(buffer, 0, frame, 0, bytesRead);

                if (resampler != null) {
                    frame = resampler.resample(frame, bytesRead);
                }

                double rms = calculateRMS(frame, frame.length);
                publish(new AudioMonitorEvent(frame, frame.length, rms, NOISE_FLOOR, RMS_THRESHOLD_HIGH));

                if (rms > RMS_THRESHOLD_HIGH) {
                    consecutiveVoice++;
                    consecutiveSilence = 0;
                    frame = Amplifier.amplify(frame);
                } else {
                    consecutiveVoice = 0;
                    consecutiveSilence++;
                }

                if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES && !isSpeaking.get()) {
                    isActive = true;
                    for (byte[] pre : preRoll) {
                        audioCollector.write(pre, 0, pre.length);
                    }
                }

                if (isActive) {
                    audioCollector.write(frame, 0, frame.length);
                }

                if (preRoll.size() >= PRE_ROLL_FRAMES) {
                    preRoll.poll();
                }
                preRoll.add(frame);

                if (isActive && consecutiveSilence >= EXIT_SILENCE_FRAMES) {
                    isActive = false;
                    consecutiveVoice = 0;
                    consecutiveSilence = 0;
                    byte[] utterance = audioCollector.toByteArray();
                    audioCollector.reset();
                    if (utterance.length >= MIN_AUDIO_BYTES) {
                        sendToNemo(utterance);
                    }
                }
            }
        } catch (LineUnavailableException e) {
            log.error("Audio line unavailable: {}", e.getMessage());
        }
    }

    private void sendToNemo(byte[] pcmBytes) {
        if (webSocket == null || webSocket.isOutputClosed()) {
            log.warn("NeMo STT: WebSocket not connected, dropping utterance");
            EventBusManager.publish(new AppLogEvent("NeMo STT: Not connected to server"));
            return;
        }
        webSocket.sendBinary(ByteBuffer.wrap(pcmBytes), true)
                .exceptionally(ex -> {
                    log.error("NeMo STT send error: {}", ex.getMessage());
                    return null;
                });
    }

    private void onTranscription(String text) {
        if (text == null || text.isBlank()) return;

        String cleaned = text.toLowerCase().trim();
        if (cleaned.contains("(") || cleaned.contains("*") || cleaned.contains("[")
                || cleaned.contains("www.") || cleaned.contains(".com")
                || cleaned.length() < 3) {
            return;
        }

        String sanitized = STTSanitizer.getInstance().correctMistakes(cleaned);
        EventBusManager.publish(new AppLogEvent("STT: [" + sanitized + "]"));

        if (systemSession.isStreamingModeOn()) {
            if (passThroughWords.stream().anyMatch(sanitized::contains)) {
                dispatch(sanitized);
            }
        } else {
            dispatch(sanitized);
        }
    }

    private void dispatch(String sanitized) {
        EventBusManager.publish(new TTSInterruptEvent());
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        EventBusManager.publish(new UserInputEvent(
                sanitized.replace("computer", "").trim(),
                1.0f
        ));
    }

    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2;
        for (int i = 0; i < length; i += 2) {
            int val = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            if (val > 32767) val -= 65536;
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }

    @Subscribe
    public void onIsSpeakingEvent(IsSpeakingEvent event) {
        isSpeaking.set(event.isSpeaking());
    }

    private class NemoListener implements WebSocket.Listener {

        private final StringBuilder textAccumulator = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textAccumulator.append(data);
            if (last) {
                onTranscription(textAccumulator.toString().trim());
                textAccumulator.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("NeMo WebSocket error: {}", error.getMessage());
            EventBusManager.publish(new AppLogEvent("NeMo STT error: " + error.getMessage()));
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("NeMo WebSocket closed: {} {}", statusCode, reason);
            return null;
        }
    }
}
