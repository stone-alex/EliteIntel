package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.*;

public class SetPowerToSystemsHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPowerToSystemsHandler.class);
    public static final int DELAY = 5;


    public SetPowerToSystemsHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {

            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseSystemsPower = INCREASE_SYSTEMS_POWER.getGameBinding();
            String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);

            log.info("Power distribution complete");
        } catch (InterruptedException e) {
            //ok
        }
    }
}
