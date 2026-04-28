package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_HARDPOINTS_TOGGLE;

public class RetractHardpointsHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if(status.isInMainShip()) {
            if (status.isHardpointsDeployed()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Hardpoints already retracted."));
            }
        }
    }
}
