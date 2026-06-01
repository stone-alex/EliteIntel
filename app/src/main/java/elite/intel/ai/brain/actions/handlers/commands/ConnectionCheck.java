package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;

public class ConnectionCheck implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        StringBuilder sb = new StringBuilder();
        sb.append(" connection check ");
        EventBusManager.publish(
                new AiVoxResponseEvent("Connection check successful.")
        );
    }
}
