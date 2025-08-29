package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;
import elite.companion.util.AIPersonality;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetPersonalityHandler implements CommandHandler{

    @Override public void handle(JsonObject params, String responseText) {
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PERSONALITY.getPlaceholder(), params);
            AIPersonality aiPersonality = AIPersonality.valueOf(jsonElement.getAsString().toUpperCase());
            SystemSession.getInstance().setAIPersonality(aiPersonality);
            VoiceGenerator.getInstance().speak(responseText);
        } catch (IllegalArgumentException e) {
            VoiceGenerator.getInstance().speak("No such personality. try Professional, Familiar or Unhinged");
        }
    }
}
