package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

/**
 * The SetStreamingModeHandler class is responsible for processing the command
 * to toggle the streaming mode within the system session. Enabling or disabling
 * streaming mode impacts how the system responds to user commands.
 * <p>
 * This handler:
 * - Extracts the streaming mode status from the provided parameters.
 * - Updates the system session to reflect the new streaming mode state.
 * - Publishes a notification event to inform users about the updated mode.
 * <p>
 * When streaming mode is enabled:
 * - Commands must be prefixed with a specific keyword (e.g., "Computer").
 * - A notification message regarding streaming mode activation is issued.
 * <p>
 * When streaming mode is disabled:
 * - The system is fully responsive without requiring command prefixes.
 * - A notification message regarding streaming mode deactivation is issued.
 * <p>
 * Implements the CommandHandler interface to ensure consistent handling
 * of incoming commands across the system.
 */
public class SetStreamingModeHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(SetStreamingModeHandler.class);

    @Override public void handle(String action, JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(Commands.SET_STREAMING_MODE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString()) || "true".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setStreamingMode(isOn);

        if (isOn) {
            EventBusManager.publish(new AiVoxResponseEvent("streaming mode is on. Please prefix your commands to me with Computer or " + systemSession.getAIVoice().getName()));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("streaming mode is off... I am listening."));
        }
    }
}
