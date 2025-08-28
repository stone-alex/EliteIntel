package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetRadioTransmissionOnOff implements CommandHandler{

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_RADIO_TRANSMISSION_MODDE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession.getInstance().updateSession(SystemSession.RADION_TRANSMISSION_ON_OFF, isOn);
        VoiceGenerator.getInstance().speak(responseText);

    }
}
