package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.SystemShutDownEvent;

public class SystemShutDownRequestHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new AiVoxResponseEvent("Shutting down systems"));
        EventBusManager.publish(new SystemShutDownEvent());
    }
}
