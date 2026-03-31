package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP_BUGGY;

public class CloseCargoScoopHandler implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            if (status.isCargoScoopDeployed()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Cargo scoop already retracted."));
            }
        }

        if (status.isInSrv()) {
            if (status.isCargoScoopDeployed()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_TOGGLE_CARGO_SCOOP_BUGGY.getGameBinding(), 0));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Cargo scoop already retracted."));
            }
        }
    }
}
