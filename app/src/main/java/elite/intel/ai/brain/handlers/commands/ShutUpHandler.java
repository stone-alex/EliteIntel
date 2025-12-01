package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;

public class ShutUpHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new TTSInterruptEvent());
    }
}
