package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.mouth.Voices;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetAiVoice implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_AI_VOICE.getPlaceholder(), params);
        if (jsonElement == null || jsonElement.getAsString().isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("Sorry, the value returned was null or empty. I am unable to process your request."));
            return;
        }
        ;
        setVoice(jsonElement);
    }

    private void setVoice(JsonElement jsonElement) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            systemSession.setAIVoice(Voices.valueOf(jsonElement.getAsString().toUpperCase()));
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new VoiceProcessEvent("Sorry, I don't understand voice name: " + jsonElement.getAsString().toUpperCase() + ". Error: " + e.getMessage()));
        }
    }
}
