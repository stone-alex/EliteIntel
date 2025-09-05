package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.SET_SPEED_ZERO;

public class PerformFSSScanHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(PerformFSSScanHandler.class);

    public PerformFSSScanHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {
            String actionSetThrottleToZero = SET_SPEED_ZERO.getGameBinding();
            String actionPressFSS = EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

            operateKeyboard(actionSetThrottleToZero, 0);
            Thread.sleep(120);
            operateKeyboard(actionPressFSS, 0);
            Thread.sleep(120);
            operateKeyboard(actionPressFSS, 4500);
        } catch (InterruptedException e) {
            log.error("Failed to perform FSS scan: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
