package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.LeftPanel;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class OpenTransactionHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInSrv() || status.isInFighter()) {
            navigator.openAndNavigate(StatusFlags.GuiFocus.EXTERNAL_PANEL, LeftPanel.TRANSACTIONS);
        } else if (status.isOnFoot()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            SleepNoThrow.sleep(500);
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        }
    }
}
