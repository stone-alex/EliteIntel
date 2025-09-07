package elite.companion.ai.brain.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.ai.mouth.GoogleVoices;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;

import static elite.companion.util.json.JsonParameterExtractor.extractParameter;

/**
 * The SetAiVoice class implements the CommandHandler interface and facilitates the
 * handling of a command to set or change the AI voice in the system.
 * <p>
 * This class processes the parameters received through a command, validates the input,
 * and updates the system's AI voice configuration. If the provided voice name is invalid
 * or null, an error event is published via the EventBusManager.
 */
public class SetAiVoice implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_AI_VOICE.getPlaceholder(), params);
        if (jsonElement == null || jsonElement.getAsString().isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("Sorry, the value returned was null or empty. I am unable to process your request."));
            return;
        }
        setVoice(jsonElement);
    }

    private void setVoice(JsonElement jsonElement) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            systemSession.setAIVoice(GoogleVoices.valueOf(jsonElement.getAsString().toUpperCase()));
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new VoiceProcessEvent("Sorry, I don't understand voice name: " + jsonElement.getAsString().toUpperCase() + ". Error: " + e.getMessage()));
        }
    }
}
