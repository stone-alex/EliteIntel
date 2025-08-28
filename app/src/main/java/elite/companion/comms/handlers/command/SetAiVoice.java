package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.comms.voice.Voices;
import elite.companion.session.SystemSession;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetAiVoice implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_AI_VOICE.getPlaceholder(), params);
        if (jsonElement == null || jsonElement.getAsString().isEmpty()) {
            VoiceGenerator.getInstance().speak("Sorry, the value returned was null or empty. I am unable to process your request.");
            return;
        }
        ;
        setVoice(jsonElement);
    }

    private void setVoice(JsonElement jsonElement) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            systemSession.setAIVoice(Voices.valueOf(jsonElement.getAsString().toUpperCase()));
            VoiceGenerator.getInstance().speak("Voice set to " + systemSession.getAIVoice().getName());
        } catch (IllegalArgumentException e) {
            VoiceGenerator.getInstance().speak("Sorry, I don't understand voice name: " + jsonElement.getAsString().toUpperCase() + ". Error: " + e.getMessage());
        }
    }
}
