package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;
import elite.companion.util.AICadence;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetCadenceHandler implements CommandHandler {
    @Override public void handle(JsonObject params, String responseText) {
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_CADENCE.getPlaceholder(), params);
            AICadence aiCadence = AICadence.valueOf(jsonElement.getAsString().toUpperCase());
            SystemSession.getInstance().setAICadence(aiCadence);
            VoiceGenerator.getInstance().speak(responseText);
        } catch (IllegalArgumentException e) {
            SystemSession.getInstance().sendToAiAnalysis("No such cadence. try Imperial, Federation or Alliance");
        }

    }
}
