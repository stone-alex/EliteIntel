package elite.companion.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.ai.brain.handlers.commands.CommandHandler;
import elite.companion.session.SystemSession;

import static elite.companion.util.json.JsonParameterExtractor.extractParameter;

public class SetRadioTransmissionOnOff implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CustomCommands.SET_RADIO_TRANSMISSION_MODDE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession.getInstance().put(SystemSession.RADION_TRANSMISSION_ON_OFF, isOn);
    }
}
