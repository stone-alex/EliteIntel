package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;

public class OpenGalaxyMapHandler extends CustomCommandOperator implements CommandHandler {

    public OpenGalaxyMapHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        try {
            String openGalaxyMap = CommandActionsGame.GameCommand.GALAXY_MAP.getGameBinding();
            operateKeyboard(openGalaxyMap, 0);
            Thread.sleep(4000);
            String uiLeft = CommandActionsGame.GameCommand.UI_LEFT.getGameBinding();
            operateKeyboard(uiLeft, 0);

            String uiRight = CommandActionsGame.GameCommand.UI_RIGHT.getGameBinding();
            operateKeyboard(uiRight, 0);
        } catch (InterruptedException oops) {
            //ok
        }

    }
}
