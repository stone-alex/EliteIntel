package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_NIGHT_VISION_TOGGLE;

public class ToggleNightVision extends CommandOperator implements CommandHandler {

    public ToggleNightVision(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        operateKeyboard(BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), 0);
    }
}
