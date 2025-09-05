package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;

import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.SET_SPEED75;

public class SetOptimalSpeedHandler extends CustomCommandOperator implements CommandHandler {

    public SetOptimalSpeedHandler(GameCommandHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        String setOptimalSpeed = SET_SPEED75.getGameBinding();
        operateKeyboard(setOptimalSpeed, 0);
    }
}
