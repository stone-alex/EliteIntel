package elite.intel.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.AuxiliaryFilesMonitor;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.JournalParser;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.*;
import elite.intel.ui.view.AppView;
import elite.intel.util.SleepNoThrow;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static elite.intel.util.StringUtls.capitalizeWords;

public class AppController implements Runnable {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final Timer logTypewriterTimer = new Timer(5, null);
    private final StringBuilder logBuffer = new StringBuilder();
    private final AtomicBoolean typewriterActive = new AtomicBoolean(false);
    AuxiliaryFilesMonitor fileMonitor = new AuxiliaryFilesMonitor();
    EarsInterface ears;
    MouthInterface mouth;
    AiCommandInterface brain;
    JournalParser journalParser = new JournalParser();
    private Thread controllerThread;
    private AppView view;

    public AppController(AppView view) {
        this.view = view;
        EventBusManager.register(this);
        this.controllerThread = new Thread(this);
        this.isRunning.set(true);
        this.controllerThread.start();
    }

    private String listVoices() {
        StringBuilder sb = new StringBuilder();
        AiVoices[] voices = AiVoices.values();
        sb.append("[");
        for (AiVoices voice : voices) {
            sb.append(capitalizeWords(voice.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }

    private String listPersonalities() {
        AIPersonality[] personalities = AIPersonality.values();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (AIPersonality personality : personalities) {
            sb.append(capitalizeWords(personality.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }

    private String listCadences() {
        AICadence[] cadences = AICadence.values();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (AICadence cadence : cadences) {
            sb.append(capitalizeWords(cadence.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }

    @Subscribe
    public void onStreamModeToggle(StreamModelToggleEvent event) {
        this.view.toggleStreamingModeCheckBox.setSelected(event.isStreaming());
        EventBusManager.publish(new ToggleStreamingModeEvent(event.isStreaming()));
    }


    @Subscribe public void toggleStreamingMode(ToggleStreamingModeEvent event) {
        appendToLog("Toggle streaming mode");
        systemSession.setStreamingMode(event.isStreaming());
        EventBusManager.publish(new AiVoxResponseEvent(event.isStreaming() ? streamingModeIsOnMessage() : streamingModeIsOffMessage()));
    }

    @Subscribe public void togglePrivacyMode(TogglePrivacyModeEvent event) {
        if (event.isEnabled()) {
            ears.stop();
        } else {
            ears.start();
        }
    }

    private String streamingModeIsOffMessage() {
        return "I am listening";
    }

    private String streamingModeIsOnMessage() {
        return "Streaming mode is On. Voice to text will still be processing, but I will not react to you. Please prefix your command with word computer";
    }


    @Subscribe private void recalibrateAudio(RecalibrateAudioEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (ears == null) {
                ears = ApiFactory.getInstance().getEarsImpl();
            }
            if (mouth == null) {
                mouth = ApiFactory.getInstance().getMouthImpl();
                mouth.start();
            }
            appendToLog("Recalibrating audio...");
            ears.stop();
            systemSession.setRmsThresholdHigh(null);
            systemSession.setRmsThresholdLow(null);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Recalibrating audio..."));
            SleepNoThrow.sleep(5000);
            ears.start();
        });
    }

    @Subscribe
    public void onSystemShutdownEvent(SystemShutDownEvent event) {
        SwingUtilities.invokeLater(() -> {
            this.view.setVisible(false);
            appendToLog("SYSTEM: Shutting down...");
            SleepNoThrow.sleep(7000);
            System.exit(0);
        });
    }

    @Subscribe void onToggleServiceEvent(ToggleServicesEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (event.isStartSercice()) {
                startStopServices();
            } else {
                stopServices();
            }
        });
    }

    private void stopServices() {
        EventBusManager.publish(new AiVoxResponseEvent("Systems offline..."));
        // Stop services
        journalParser.stop();
        fileMonitor.stop();
        brain.stop();
        ears.stop();
        mouth.stop();
        systemSession.clearChatHistory();
        EventBusManager.publish(new ServicesStateEvent(false));
        isRunning.set(false);
        if (controllerThread != null) {
            controllerThread.interrupt();
        }
    }


    private void appendToLog(String data) {
        String formattedTime = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"));
        EventBusManager.publish(new AppLogEvent(formattedTime + ": " + data));
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    @Subscribe
    public void onAppLogEvent(AppLogEvent event) {
        String line = "\n"+event.getData();
        if (line == null || line.isBlank() || this.view.logArea == null) return;

        synchronized (logBuffer) {
            logBuffer.append(line);
        }

        if (typewriterActive.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(this::startTypewriter);
        }
    }

    private void startTypewriter() {
        logTypewriterTimer.stop();
        logTypewriterTimer.addActionListener(e -> {
            String nextChar;
            synchronized (logBuffer) {
                if (logBuffer.length() == 0) {
                    logTypewriterTimer.stop();
                    typewriterActive.set(false);
                    return;
                }
                nextChar = String.valueOf(logBuffer.charAt(0));
                logBuffer.deleteCharAt(0);
            }

            try {
                int pos = this.view.logArea.getDocument().getLength();
                this.view.logArea.getDocument().insertString(pos, nextChar, null);
                this.view.logArea.setCaretPosition(pos + 1);
                this.view.logArea.repaint();
            } catch (BadLocationException ex) {
                logTypewriterTimer.stop();
                typewriterActive.set(false);
            }
        });
        logTypewriterTimer.start();
    }



    private void startStopServices() {
        String ttsApiKey = SystemSession.getInstance().getTtsApiKey();
        if (ttsApiKey == null || ttsApiKey.trim().isEmpty() || ttsApiKey.equals("null")) {
            appendToLog("SYSTEM: TTS API key is not provided. I have no mouth to speak with");
            return;
        }

        String sttApiKey = SystemSession.getInstance().getSttApiKey();
        if (sttApiKey == null || sttApiKey.trim().isEmpty() || sttApiKey.equals("null")) {
            appendToLog("SYSTEM: STT API key is not provided. I have no ears to hear with");
            return;
        }

        String aiApiKey = SystemSession.getInstance().getAiApiKey();
        if (aiApiKey == null || aiApiKey.trim().isEmpty() || aiApiKey.equals("null")) {
            appendToLog("SYSTEM: AI API key is not provided. I have no brain to process with");
            return;
        }

        systemSession.clearChatHistory();
        journalParser.start();
        fileMonitor.start();

        mouth = ApiFactory.getInstance().getMouthImpl();
        mouth.start();

        ears = ApiFactory.getInstance().getEarsImpl();
        ears.start();

        brain = ApiFactory.getInstance().getCommandEndpoint();
        brain.start();

        String mission_statement = playerSession.getPlayerMissionStatement();
        playerSession.setPlayerMissionStatement(mission_statement);

        appendToLog("Available voices: " + listVoices());
        appendToLog("Available personalities: " + listPersonalities());
        appendToLog("Available profiles: " + listCadences());
        EventBusManager.publish(new ServicesStateEvent(true));
    }
}