package elite.intel.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.ears.AudioCalibrator;
import elite.intel.ai.ears.AudioFormatDetector;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.hands.KeyBindCheck;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.*;
import elite.intel.gameapi.journal.MissingMissionMonitor;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.*;
import elite.intel.ui.view.AppView;
import elite.intel.util.Updater;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static elite.intel.util.StringUtls.capitalizeWords;

public class AppController implements Runnable {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean showDetailedLog = new AtomicBoolean(false);
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final Timer logTypewriterTimer = new Timer(5, null);
    private final StringBuilder logBuffer = new StringBuilder();
    private final AtomicBoolean typewriterActive = new AtomicBoolean(false);
    private final AppView view;

    /// NOTE Order of services is important
    private final Map<ServiceType, ServiceHolder> services = new LinkedHashMap<>();


    public AppController(AppView view) {
        this.view = view;
        EventBusManager.register(this);
        this.isRunning.set(false);
        startIfWeHaveCredentials();
        checkForUpdates();
    }

    private void checkForUpdates() {
        SwingUtilities.invokeLater(() -> {
            CompletableFuture<Boolean> checkAsync = Updater.isUpdateAvailableAsync();
            try {
                Boolean updateAvailable = checkAsync.get();
                if (updateAvailable) {
                    EventBusManager.publish(new UpdateAvailableEvent());
                }
            } catch (Exception e) {
                //kek
            }
        });

    }

    private void startIfWeHaveCredentials() {
        /// auto start session if we have credentials.
        if (!systemSession.getSttApiKey().isEmpty()) {
            EventBusManager.publish(new ToggleServicesEvent(true));
        }
    }

    private String listVoices() {
        if (systemSession.isRunningPiperTts()) {
            return ""; // Voice choice is not available with Piper TTS
        }
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

    @Subscribe public void onSpeechSpeedChangeEvent(SpeechSpeedChangeEvent event) {
        systemSession.setSpeechSpeed(event.getSpeed());
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
        EarsInterface ears = services.get(ServiceType.EARS).get();
        if (ears != null) {
            if (event.isEnabled()) {
                ears.stop();
            } else {
                ears.start();
            }
        }
    }

    private String streamingModeIsOffMessage() {
        return "I am listening";
    }

    private String streamingModeIsOnMessage() {
        return "Streaming mode is On. Prefix your command with word computer";
    }


    @Subscribe private void recalibrateAudio(RecalibrateAudioEvent event) {
        SwingUtilities.invokeLater(() -> {
            appendToLog("Starting audio calibration...");
            // Stop normal listening
            EarsInterface ears = services.get(ServiceType.EARS).get();
            if (ears == null) return;

            ears.stop();

            new Thread(() -> {
                try {
                    AudioFormatDetector.Format format = AudioFormatDetector.detectSupportedFormat();
                    AudioCalibrator.calibrateRMS(format.getSampleRate(), format.getBufferSize());

                    // Back to EDT: restart ears + success
                    SwingUtilities.invokeLater(() -> {
                        ears.start();
                        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Audio calibration complete"));
                        appendToLog("Calibration complete: HIGH=" +
                                SystemSession.getInstance().getRmsThresholdHigh() +
                                " LOW=" + SystemSession.getInstance().getRmsThresholdLow());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        ears.start(); // always restart on way out
                        appendToLog("Calibration failed: " + ex.getMessage());
                        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Audio calibration failed"));
                    });
                }
            }, "AudioCalibrator-Thread").start();
        });
    }

    @Subscribe
    public void onSystemShutdownEvent(SystemShutDownEvent event) {
        SwingUtilities.invokeLater(() -> {
            this.view.setVisible(false);
            appendToLog("SYSTEM: Shutting down...");
            System.exit(0);
        });
    }

    @Subscribe void onToggleServiceEvent(ToggleServicesEvent event) {
        new Thread(() -> {
            if (event.isStartSercice()) {
                startServices();
            } else {
                stopServices();
            }
        }).start();
    }


    @Subscribe void onToggleSendMarketDataEvent(ToggleSendMarketDataEvent event) {
        SwingUtilities.invokeLater(() -> systemSession.setSendMarketData(event.isEnabled()));
    }

    @Subscribe void onToggleSendOutfittingDataEvent(ToggleSendOutfittingDataEvent event) {
        SwingUtilities.invokeLater(() -> systemSession.setSendOutfittingData(event.isEnabled()));
    }

    @Subscribe void onToggleSendShipyardDataEvent(ToggleSendShipyardDataEvent event) {
        SwingUtilities.invokeLater(() -> systemSession.setSendShipyardDataEvent(event.isEnabled()));
    }

    @Subscribe void onToggleSendExplorationData(ToggleSendExplorationDataEvent event) {
        SwingUtilities.invokeLater(() -> systemSession.setExplorationData(event.isEnabled()));
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
                //noinspection BusyWait
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Subscribe public void onToggleDetailedLogEvent(ToggleDetailedLogEvent event) {
        showDetailedLog.set(event.isDetailed());
    }

    @Subscribe public void onAppLogDebugEvent(AppLogDebugEvent event) {
        if (showDetailedLog.get()) {
            String line = "\n" + event.getData();
            if (line.isBlank() || this.view.logArea == null) return;

            synchronized (logBuffer) {
                logBuffer.append(line);
            }

            if (typewriterActive.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(this::startTypewriter);
            }
        }
    }

    @Subscribe
    public void onAppLogEvent(AppLogEvent event) {
        String line = "\n" + event.getData();
        if (line.isBlank() || this.view.logArea == null) return;

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
                if (logBuffer.isEmpty()) {
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

    private void startServices() {
        if (isRunning.get()) return;
        EventBusManager.publish(new ClearConsoleEvent());
        /// NOTE: User can swap keys. the services MUST be re-initialized before we start them.
        initServices();

        systemSession.clearChatHistory();

        for (ServiceType type : ServiceType.values()) {
            ServiceHolder service = services.get(type);
            if (service != null) {
                service.start();
            }
        }

        String mission_statement = playerSession.getPlayerMissionStatement();
        playerSession.setPlayerMissionStatement(mission_statement);

        if (!systemSession.useLocalTTS()) {
            appendToLog("Available voices:\n" + listVoices());
        }
        if (!systemSession.useLocalQueryLlm() && !systemSession.isRunningPiperTts()) {
            appendToLog("Available personalities:\n" + listPersonalities());
            appendToLog("Available profiles:\n" + listCadences());
        }

        isRunning.set(true);
        EventBusManager.publish(new ServicesStateEvent(true));

        Timer connectionCheckTimer = new Timer(2000, e -> {
            EventBusManager.publish(new AiVoxResponseEvent("Checking LLM Connection..."));
            EventBusManager.publish(new UserInputEvent("Verify LLM Connection", 100f));
        });
        connectionCheckTimer.setRepeats(false);
        connectionCheckTimer.start();
        
        
        KeyBindCheck.getInstance().check();
    }

    private void stopServices() {
        if (!isRunning.get()) return;

        EventBusManager.publish(new AiVoxResponseEvent("Shutting Down..."));

        // Stop in reverse dependency order
        List<ServiceType> reverseOrder = new ArrayList<>(services.keySet());
        Collections.reverse(reverseOrder);

        for (ServiceType type : reverseOrder) {
            ServiceHolder holder = services.get(type);
            if (holder != null) {
                holder.stop();
                services.remove(type);
            }
        }
        this.services.clear();

        systemSession.clearChatHistory();
        EventBusManager.publish(new ServicesStateEvent(false));
        isRunning.set(false);
        EventBusManager.publish(new ClearConsoleEvent());
        EventBusManager.publish(new AppLogEvent("All services are stopped\n\n"));
    }

    private void initServices() {
        /// NOTE Order is important.
        stopServices();
        this.services.clear();
        services.put(ServiceType.JOURNAL_PARSER, new ServiceHolder(JournalParser::new));
        services.put(ServiceType.AUXILIARY_FILES_MONITOR, new ServiceHolder(AuxiliaryFilesMonitor::new));
        services.put(ServiceType.MOUTH, new ServiceHolder(ApiFactory.getInstance()::getMouthImpl));
        services.put(ServiceType.EARS, new ServiceHolder(ApiFactory.getInstance()::getEarsImpl));
        services.put(ServiceType.BRAIN, new ServiceHolder(ApiFactory.getInstance()::getCommandEndpoint));
        services.put(ServiceType.NOTIFICATION_MONITOR, new ServiceHolder(DeferredNotificationMonitor::getInstance));
        services.put(ServiceType.MISSING_MISSION_MONITOR, new ServiceHolder(MissingMissionMonitor::getInstance));
    }

    private static class ServiceHolder {
        private final Supplier<? extends ManagedService> creator;
        private ManagedService instance;

        ServiceHolder(Supplier<? extends ManagedService> creator) {
            this.creator = Objects.requireNonNull(creator);
        }

        void start() {
            if (instance == null) {
                instance = creator.get();
            }
            if (instance != null) {
                instance.start();
            }
        }

        void stop() {
            if (instance != null) {
                instance.stop();
                instance = null;
            }
        }


        @SuppressWarnings("unchecked")
        <T extends ManagedService> T get() {
            return (T) instance;
        }
    }

    private enum ServiceType {
        JOURNAL_PARSER,
        AUXILIARY_FILES_MONITOR,
        MOUTH,
        EARS,
        BRAIN,
        NOTIFICATION_MONITOR,
        MISSING_MISSION_MONITOR
    }
}