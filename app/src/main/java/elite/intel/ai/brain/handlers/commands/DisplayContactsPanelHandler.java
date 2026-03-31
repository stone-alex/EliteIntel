package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL_BUGGY;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL;

public class DisplayContactsPanelHandler implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_FOCUS_LEFT_PANEL.getGameBinding(), 0));
        }

        if (status.isInSrv()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_FOCUS_CONTACTS_PANEL_BUGGY.getGameBinding(), 0));
        }
    }
}
