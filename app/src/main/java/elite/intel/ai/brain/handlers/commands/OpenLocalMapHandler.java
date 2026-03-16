package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class OpenLocalMapHandler extends CommandOperator implements CommandHandler {


    public OpenLocalMapHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();
        Status status = Status.getInstance();
        if (status.isInMainShip()) {
            operateKeyboard(BINDING_LOCAL_MAP.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_LOCAL_MAP_BUGGY.getGameBinding(), 0);
        }

        if (status.isOnFoot()) {
            operateKeyboard(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500);
            operateKeyboard(BINDING_UI_LEFT.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_RIGHT.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
        }
    }
}
