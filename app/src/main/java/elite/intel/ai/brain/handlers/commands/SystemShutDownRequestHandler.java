package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.SystemShutDownEvent;
import elite.intel.ui.event.ToggleServicesEvent;
import elite.intel.util.SleepNoThrow;

public class SystemShutDownRequestHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new SystemShutDownEvent());
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Shutting down"));
        SleepNoThrow.sleep(7000);
        EventBusManager.publish(new ToggleServicesEvent(false));
    }
}
