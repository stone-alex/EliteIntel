package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_HARDPOINTS_TOGGLE;

public class DeployHardpointsHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isHardpointsDeployed()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Hardpoints already deployed"));

        } else {
            GameControllerBus.publish(new GameInputEvent(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0));
        }
    }
}
