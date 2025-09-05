package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.*;

public class SetPowerToEnginesHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPowerToEnginesHandler.class);
    public static final int DELAY = 5;

    public SetPowerToEnginesHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        try {
            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseEnginePower = INCREASE_ENGINES_POWER.getGameBinding();
            String increaseSystemPower = INCREASE_SYSTEMS_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            log.info("Diverting power to engines");
        } catch (InterruptedException e) {
            //ok
        }
    }
}
