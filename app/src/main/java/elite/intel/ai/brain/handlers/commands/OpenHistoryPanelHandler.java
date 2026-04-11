package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CommsPanel;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class OpenHistoryPanelHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInFighter()) {
            navigator.openAndNavigate(StatusFlags.GuiFocus.COMMS_PANEL, CommsPanel.HISTORY);
        } else if (status.isOnFoot()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            SleepNoThrow.sleep(500);
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_UP.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I can't do that right now."));
        }
    }
}
