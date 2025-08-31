package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.comms.voice.VoiceToAllegiances;
import elite.companion.comms.voice.Voices;
import elite.companion.session.SystemSession;
import elite.companion.comms.ai.AICadence;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetCadenceHandler implements CommandHandler {
    @Override public void handle(JsonObject params, String responseText) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_CADENCE.getPlaceholder(), params);
            AICadence aiCadence = AICadence.valueOf(jsonElement.getAsString().toUpperCase());

            Voices currentVoice = systemSession.getAIVoice();
            VoiceToAllegiances.getInstance().getVoiceForCadence(aiCadence, currentVoice);

            systemSession.setAICadence(aiCadence);
        } catch (IllegalArgumentException e) {
            systemSession.sendToAiAnalysis("No such cadence. try Imperial, Federation or Alliance");
        }

    }
}
