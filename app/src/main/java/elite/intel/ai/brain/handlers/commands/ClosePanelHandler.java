package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXIT_KEY;

public class ClosePanelHandler extends CommandOperator implements CommandHandler {

    public ClosePanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();

        for (int i = 0; i < 10; i++) { ///  back out of all menus etc
            operateKeyboard(BINDING_EXIT_KEY.getGameBinding(), 0);
        }
    }
}