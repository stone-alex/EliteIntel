package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

public class SetRadioTransmissionOnOff implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CustomCommands.SET_RADIO_TRANSMISSION_MODDE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        PlayerSession playerSession = PlayerSession.getInstance();
        //TODO: FIX ME
    }
}
