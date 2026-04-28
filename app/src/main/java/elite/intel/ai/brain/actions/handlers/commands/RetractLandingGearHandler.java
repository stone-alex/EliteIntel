package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_LANDING_GEAR_TOGGLE;

public class RetractLandingGearHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isDocked() || status.isLanded() || status.isOnFoot() || status.isInFighter()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Can not do that right now."));
            return;
        }

        if (status.isInMainShip() && status.isLandingGearDown()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Landing gear already retracted."));
        }
    }
}
