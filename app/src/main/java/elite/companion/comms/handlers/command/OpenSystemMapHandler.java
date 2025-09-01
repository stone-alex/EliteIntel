package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;

public class OpenSystemMapHandler extends CustomCommandOperator implements CommandHandler {

    public OpenSystemMapHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        String openMap = CommandActionsGame.GameCommand.SYSTEM_MAP.getGameBinding();
        operateKeyboard(openMap, 0);
    }
}
