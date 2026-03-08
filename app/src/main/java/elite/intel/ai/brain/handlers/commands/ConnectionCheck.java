package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;

public class ConnectionCheck implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ping ");
        String instructions = "Acknowledge LLM Model and number of parameters. Data is presented to you as (LLMName:120b) Example: LLM On Line";
        EventBusManager.publish(
                new SensorDataEvent(
                        sb.toString(),
                        instructions
                )
        );
    }
}
