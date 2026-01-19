package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.session.SystemSession;

/**
 * The SetPersonalityHandler class implements the CommandHandler interface and
 * handles the logic for setting the AI personality in the system session.
 * This handler processes requests to change the AI's response style and tone
 * based on predefined personality types.
 * <p>
 * The class retrieves the personality parameter from the provided JSON object.
 * If a valid parameter is found, it is converted to the corresponding
 * AIPersonality enum value, and the system's AI personality is updated.
 * If the parameter is invalid or does not match any predefined personality,
 * a notification is published to inform the user.
 * <p>
 * Key Responsibilities:
 * - Extracts the personality parameter from the JSON object.
 * - Validates and converts the parameter to an AIPersonality enum value.
 * - Updates the system's AI personality in the session.
 * - Publishes an error event if the provided personality is invalid.
 * <p>
 * This handler is specifically associated with the SET_PERSONALITY command action.
 */
public class SetPersonalityHandler implements CommandHandler {

    private final SystemSession systemSession = SystemSession.getInstance();
    @Override public void handle(String action, JsonObject params, String responseText) {
        try {
            String keyValue = params.get("key").getAsString();

            AIPersonality aiPersonality = AIPersonality.valueOf(keyValue.toUpperCase());

            systemSession.setAIPersonality(aiPersonality);
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new SensorDataEvent("No such personality. try Professional, Casual, Friendly, Unhinged or Rogue"));
        }
    }
}
