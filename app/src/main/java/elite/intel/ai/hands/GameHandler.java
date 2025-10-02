package elite.intel.ai.hands;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The GameHandler class is responsible for managing game-related commands and actions,
 * including execution of bindings, monitoring of key bindings, and handling AI command
 * responses. It acts as a central component that interacts with multiple subsystems
 * like BindingsMonitor, KeyBindingExecutor, and EventBusManager.
 *
 * The GameHandler initializes the necessary resources and starts a processing thread
 * to handle gameplay-related operations. It also provides mechanisms to stop the thread
 * and clean up resources when necessary.
 */
public class GameHandler {
    private static final Logger log = LogManager.getLogger(GameHandler.class);
    private final KeyBindingExecutor executor;
    private final BindingsMonitor monitor;
    private Thread processingThread;
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public GameHandler() throws Exception {
        this.executor = KeyBindingExecutor.getInstance();
        this.monitor = BindingsMonitor.getInstance();
        log.info("GameCommandHandler initialized");
    }

    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("GameCommandHandler is already running");
            return;
        }
        processingThread = new Thread(this::run, "GameCommandHandlerThread");
        processingThread.start();
        log.info("GameCommandHandler started");
    }

    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("GameCommandHandler is not running");
            return;
        }
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
            EventBusManager.publish(new VocalisationRequestEvent("Error in command handler: " + e.getMessage()));
        }
    }


    /**
     * Processes the AI-generated response received in the form of a JSON object.
     * Depending on the type of response, this method either handles a command or logs an error.
     * If the response type is identified as a "command," it extracts the action and parameters
     * from the JSON object and delegates handling to the appropriate command handler.
     * In case of an unrecognized response type, it logs a warning and provides feedback via chat handling.
     * Any exceptions encountered during processing are logged and handled appropriately.
     *
     * @param jsonResponse the JSON object containing the AI-generated response. It is expected
     *                     to contain keys such as "type", "response_text", "action", and "params".
     *                     The "type" value determines how the response is processed. If the type is
     *                     "command", additional fields like "action" and "params" are used for further processing.
     */
    public void processAiCommand(JsonObject jsonResponse) {
        try {
            String type = jsonResponse.has("type") ? jsonResponse.get("type").getAsString() : "";
            String responseText = jsonResponse.has("response_text") ? jsonResponse.get("response_text").getAsString() : "";

            if (type.equalsIgnoreCase("command")) {
                String action = jsonResponse.has("action") ? jsonResponse.get("action").getAsString() : "";
                JsonObject params = jsonResponse.has("params") ? jsonResponse.get("params").getAsJsonObject() : new JsonObject();
                handleCommand(action, params, responseText);
            } else {
                log.warn("Unknown response type: {}", type);
                handleChat(responseText + " GameCommandHandler - command handler not found for type: " + type + " check programming");
            }
        } catch (Exception e) {
            log.error("Failed to process AI response: {}", e.getMessage(), e);
            handleChat("GameCommandHandler - Error processing command.");
        }
    }

    private void handleCommand(String action, JsonObject params, String responseText) {
        if (BLACKLISTED_ACTIONS.contains(action) || action.startsWith("Humanoid")) {
            log.warn("Attempted to execute blacklisted or Humanoid action: {}", action);
            handleChat("Sorry, I am not allowed to execute that action.");
            return;
        }

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setAction(action);
        systemSession.setParams(params);
        log.debug("Updated SessionTracker with action: {}, params: {}", action, params);
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(GameCommands.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBinding(binding);
            log.info("Executed action: {} with key: {}", action, binding.key);
        } else {
            log.warn("No binding found for action: {}", action);
            handleChat("No key binding found for that action.");
        }
    }

    private void handleChat(String responseText) {
        EventBusManager.publish(new VocalisationRequestEvent(responseText));
        log.info("Sent to VoiceGenerator: {}", responseText);
    }

    public BindingsMonitor getMonitor() {
        return monitor;
    }

    public KeyBindingExecutor getExecutor() {
        return executor;
    }
}