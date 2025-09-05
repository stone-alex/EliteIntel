package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.brain.AICadence;
import elite.companion.comms.mouth.VoiceToAllegiances;
import elite.companion.comms.mouth.Voices;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetCadenceHandler implements CommandHandler {
    @Override public void handle(JsonObject params, String responseText) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PROFILE.getPlaceholder(), params);
            AICadence aiCadence = AICadence.valueOf(jsonElement.getAsString().toUpperCase());

            Voices currentVoice = systemSession.getAIVoice();
            VoiceToAllegiances.getInstance().getVoiceForCadence(aiCadence, currentVoice);

            systemSession.setAICadence(aiCadence);
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new SensorDataEvent("No such cadence. try Imperial, Federation or Alliance"));
        }

    }
}
