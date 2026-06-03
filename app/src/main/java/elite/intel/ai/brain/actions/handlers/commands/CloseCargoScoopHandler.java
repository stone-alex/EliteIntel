package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP;
import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP_BUGGY;

public class CloseCargoScoopHandler implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            if (status.isCargoScoopDeployed()) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding())));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Cargo scoop already retracted."));
            }
        }

        if (status.isInSrv()) {
            if (status.isCargoScoopDeployed()) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_TOGGLE_CARGO_SCOOP_BUGGY.getGameBinding())));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Cargo scoop already retracted."));
            }
        }
    }
}
