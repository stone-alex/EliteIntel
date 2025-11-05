package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.StreamModelTogleEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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

        boolean isOn = params.get("state").getAsBoolean();
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setStreamingMode(isOn);

        if (isOn) {
            EventBusManager.publish(new StreamModelTogleEvent(true));
        } else {
            EventBusManager.publish(new StreamModelTogleEvent(false));
        }
    }
}
