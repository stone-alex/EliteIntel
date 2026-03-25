package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXIT_KEY;

public class ClosePanelHandler extends CommandOperator implements CommandHandler {

    public ClosePanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();

        if (status.isSystemMapOpen() || status.isGalaxyMapOpen()) {
            operateKeyboard(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0);
        }
        if (status.isGalaxyMapOpen() || status.isSystemMapOpen() || status.isStationServicesOpen() || status.isOrreryOpen()) {
            for (int i = 0; i < 10; i++) { ///  back out of all menus etc
                operateKeyboard(BINDING_EXIT_KEY.getGameBinding(), 0);
            }
        }
    }
}