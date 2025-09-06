package elite.companion.comms.brain.robot;

import com.google.gson.JsonObject;
import elite.companion.comms.handlers.command.CommandActionsGame;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The GameCommandHandler class manages game command execution, monitoring of key bindings,
 * and handles responses received from external input systems such as JSON-based AI responses.
 * It handles the lifecycle of processing threads and coordinates command execution through key bindings.
 */
public class GameCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(GameCommandHandler.class);
    private final KeyBindingsParser parser;
    private final KeyBindingExecutor executor;
    private final BindingsMonitor monitor;
    private Thread processingThread;
    private volatile boolean running;
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public GameCommandHandler() throws Exception {
        this.parser = KeyBindingsParser.getInstance();
        this.executor = KeyBindingExecutor.getInstance();
        this.monitor = BindingsMonitor.getInstance();
        log.info("GameCommandHandler initialized");
    }

    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("GameCommandHandler is already running");
            return;
        }
        running = true;
        processingThread = new Thread(this::run, "GameCommandHandlerThread");
        processingThread.start();
        log.info("GameCommandHandler started");
    }

    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("GameCommandHandler is not running");
            return;
        }
        running = false;
        processingThread.interrupt();
        try {
            monitor.stopMonitoring();
        } catch (Exception e) {
            log.error("Error stopping monitoring: {}", e.getMessage(), e);
        }

        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("GameCommandHandler stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for GameCommandHandler to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        processingThread = null;
    }

    private void run() {
        try {
            monitor.startMonitoring();
        } catch (IOException e) {
            log.info("GameCommandHandler interrupted, shutting down");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            log.error("Error in GameCommandHandler: {}", e.getMessage(), e);
            EventBusManager.publish(new VoiceProcessEvent("Error in command handler: " + e.getMessage()));
        }
    }

    public void handleGrokResponse(JsonObject jsonResponse) {
        try {
            String type = jsonResponse.has("type") ? jsonResponse.get("type").getAsString() : "";
            String responseText = jsonResponse.has("response_text") ? jsonResponse.get("response_text").getAsString() : "";

            switch (type.toLowerCase()) {
                case "command":
                    String action = jsonResponse.has("action") ? jsonResponse.get("action").getAsString() : "";
                    JsonObject params = jsonResponse.has("params") ? jsonResponse.get("params").getAsJsonObject() : new JsonObject();
                    handleCommand(action, params, responseText);
                    break;
                case "system_command":
                    JsonObject system_params = jsonResponse.has("params") ? jsonResponse.get("params").getAsJsonObject() : new JsonObject();
                    handleSystemCommand(system_params, responseText);
                    break;
                case "query":
                    handleQuery(responseText);
                    break;
                case "chat":
                    handleChat(responseText);
                    break;
                default:
                    log.warn("Unknown response type: {}", type);
                    handleChat("I'm not sure what you meant. Please try again.");
            }
        } catch (Exception e) {
            log.error("Failed to process AI response: {}", e.getMessage(), e);
            handleChat("Error processing command.");
        }
    }

    private void handleCommand(String action, JsonObject params, String responseText) {
        if (BLACKLISTED_ACTIONS.contains(action) || action.startsWith("Humanoid")) {
            log.warn("Attempted to execute blacklisted or Humanoid action: {}", action);
            handleChat("Sorry, I am not allowed to execute that action.");
            return;
        }

        SystemSession.getInstance().put("action", action);
        SystemSession.getInstance().put("params", params);
        log.debug("Updated SessionTracker with action: {}, params: {}", action, params);
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(CommandActionsGame.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBinding(binding);
            log.info("Executed action: {} with key: {}", action, binding.key);
        } else {
            log.warn("No binding found for action: {}", action);
            handleChat("No key binding found for that action.");
        }
    }

    private void handleSystemCommand(JsonObject params, String responseText) {
        SystemSession.getInstance().put("params", params);
        handleChat(responseText);
    }

    private void handleQuery(String responseText) {
        handleChat(responseText);
        log.info("Handled query response: {}", responseText);
    }

    private void handleChat(String responseText) {
        EventBusManager.publish(new VoiceProcessEvent(responseText));
        log.info("Sent to VoiceGenerator: {}", responseText);
    }

    public BindingsMonitor getMonitor() {
        return monitor;
    }

    public KeyBindingExecutor getExecutor() {
        return executor;
    }
}