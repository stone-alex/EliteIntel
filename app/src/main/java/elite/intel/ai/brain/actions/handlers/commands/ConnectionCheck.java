package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.util.StringUtls;

public class ConnectionCheck implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        StringBuilder sb = new StringBuilder();
        sb.append(" connection check ");
        String instructions = "Acknowledge successful connection with exactly this text: "
                + StringUtls.localizedSpeech("speech.connectionSuccessful");
        EventBusManager.publish(
                new SensorDataEvent(
                        sb.toString(),
                        instructions
                )
        );
    }
}
