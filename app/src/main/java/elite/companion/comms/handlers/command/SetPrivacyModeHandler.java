package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.util.json.JsonParameterExtractor.extractParameter;

/**
 * The SetPrivacyModeHandler class is responsible for processing the command
 * to toggle the privacy mode within the system session. Enabling or disabling
 * privacy mode impacts how the system responds to user commands.
 * <p>
 * This handler:
 * - Extracts the privacy mode status from the provided parameters.
 * - Updates the system session to reflect the new privacy mode state.
 * - Publishes a notification event to inform users about the updated mode.
 * <p>
 * When privacy mode is enabled:
 * - Commands must be prefixed with a specific keyword (e.g., "Computer").
 * - A notification message regarding privacy mode activation is issued.
 * <p>
 * When privacy mode is disabled:
 * - The system is fully responsive without requiring command prefixes.
 * - A notification message regarding privacy mode deactivation is issued.
 * <p>
 * Implements the CommandHandler interface to ensure consistent handling
 * of incoming commands across the system.
 */
public class SetPrivacyModeHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetRouteHandler.class);

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PRIVACY_MODE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setPrivacyMode(isOn);

        if (isOn) {
            EventBusManager.publish(new VoiceProcessEvent("Privacy mode is on. Please prefix your commands to me with Computer or " + systemSession.getAIVoice().getName()));
        } else {
            EventBusManager.publish(new VoiceProcessEvent("Privacy mode is off... I am listening."));
        }
    }
}
