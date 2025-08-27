package elite.companion.comms.ai.robot;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.GameCommandMapping;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VoiceCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(VoiceCommandHandler.class);
    private final KeyBindingsParser parser;
    private final KeyBindingExecutor executor;
    private final BindingsMonitor monitor;
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public VoiceCommandHandler() throws Exception {
        this.parser = new KeyBindingsParser();
        this.executor = new KeyBindingExecutor();
        this.monitor = new BindingsMonitor(parser);
        log.info("VoiceCommandHandler initialized");
    }

    public void start() {
        new Thread(() -> {
            try {
                monitor.startMonitoring();
            } catch (Exception e) {
                log.error("Error in bindings monitor: {}", e.getMessage());
            }
        }).start();
        log.info("Started VoiceCommandHandler monitoring");
    }

    public void stop() {
        try {
            monitor.stopMonitoring();
            log.info("Stopped VoiceCommandHandler monitoring");
        } catch (Exception e) {
            log.error("Error stopping bindings monitor: {}", e.getMessage());
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
            log.error("Failed to process Grok response: {}", e.getMessage());
            handleChat("Error processing command.");
        }
    }

    private void handleCommand(String action, JsonObject params, String responseText) {
        if (BLACKLISTED_ACTIONS.contains(action) || action.startsWith("Humanoid")) {
            log.warn("Attempted to execute blacklisted or Humanoid action: {}", action);
            handleChat("Sorry, I am not allowed to execute that action.");
            return;
        }

        SystemSession.getInstance().updateSession("action", action);
        SystemSession.getInstance().updateSession("params", params);
        log.debug("Updated SessionTracker with action: {}, params: {}", action, params);
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(GameCommandMapping.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBinding(binding);
            log.info("Executed action: {} with key: {}", action, binding.key);
            handleChat(responseText);
        } else {
            handleChat(responseText);
            log.warn("No binding found for action: {}", action);
            //handleChat("No key binding found for that action.");
        }
    }

    private void handleSystemCommand(JsonObject params, String responseText) {
        SystemSession.getInstance().updateSession("params", params);
        handleChat(responseText);
    }

    private void handleQuery(String responseText) {
        handleChat(responseText);
        log.info("Handled query response: {}", responseText);
    }

    private void handleChat(String responseText) {
        VoiceGenerator.getInstance().speak(responseText);
        log.info("Sent to VoiceGenerator: {}", responseText);
    }
}