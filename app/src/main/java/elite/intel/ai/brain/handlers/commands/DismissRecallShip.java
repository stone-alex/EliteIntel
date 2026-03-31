package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class DismissRecallShip implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSrv()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_RECALL_DISMISS_SHIP.getGameBinding(), 0));
        } else if (status.isOnFoot()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_UP.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_EXIT_KEY.getGameBinding(), 0));
        } else if (status.isInMainShip()) {
            EventBusManager.publish(new AiVoxResponseEvent("Unable to comply. You have the deck."));
            return;
        }
        if (status.isLanded()) {
            EventBusManager.publish(new AiVoxResponseEvent("Going to orbit, call me, when you need me."));
            return;
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Coming back to get you."));
            return;
        }
    }
}
