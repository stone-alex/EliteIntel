package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.brain.AIPersonality;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.session.SystemSession;

import static elite.companion.util.json.JsonParameterExtractor.extractParameter;

public class SetPersonalityHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PERSONALITY.getPlaceholder(), params);
            AIPersonality aiPersonality = AIPersonality.valueOf(jsonElement.getAsString().toUpperCase());
            SystemSession.getInstance().setAIPersonality(aiPersonality);
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new SensorDataEvent("No such personality. try Professional, Friendly, Unhinged or Rogue"));
        }
    }
}
