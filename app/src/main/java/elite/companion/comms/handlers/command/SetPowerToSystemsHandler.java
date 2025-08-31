package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetPowerToSystemsHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPowerToSystemsHandler.class);
    public static final int DELAY = 25;
    private final GameCommandHandler _gameCommandHandler;

    public SetPowerToSystemsHandler(GameCommandHandler gameCommandHandler) {
        this._gameCommandHandler = gameCommandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {

            JsonObject resetPowerJson = new JsonObject();
            resetPowerJson.addProperty("type", "command");
            resetPowerJson.addProperty("action", CommandActionsGame.GameCommand.RESET_POWER_DISTRIBUTION.getGameBinding());
            _gameCommandHandler.handleGrokResponse(resetPowerJson);
            log.info("Resetting power distribution");
            Thread.sleep(DELAY);


            JsonObject stepOne = new JsonObject();
            stepOne.addProperty("type", "command");
            stepOne.addProperty("action", CommandActionsGame.GameCommand.INCREASE_SYSTEMS_POWER.getGameBinding());
            _gameCommandHandler.handleGrokResponse(stepOne);
            log.info("Diverting power step 1");
            Thread.sleep(DELAY);

            JsonObject stepTwo = new JsonObject();
            stepTwo.addProperty("type", "command");
            stepTwo.addProperty("action", CommandActionsGame.GameCommand.INCREASE_ENGINES_POWER.getGameBinding());
            _gameCommandHandler.handleGrokResponse(stepTwo);
            log.info("Diverting power step 2");
            Thread.sleep(DELAY);

            JsonObject stepThree = new JsonObject();
            stepThree.addProperty("type", "command");
            stepThree.addProperty("action", CommandActionsGame.GameCommand.INCREASE_SYSTEMS_POWER.getGameBinding());
            _gameCommandHandler.handleGrokResponse(stepThree);
            log.info("Diverting power step 3");
            Thread.sleep(DELAY);

            JsonObject stepFour = new JsonObject();
            stepFour.addProperty("type", "command");
            stepFour.addProperty("action", CommandActionsGame.GameCommand.INCREASE_ENGINES_POWER.getGameBinding());
            _gameCommandHandler.handleGrokResponse(stepFour);
            log.info("Diverting power step 4");
            Thread.sleep(DELAY);

            JsonObject stepFive = new JsonObject();
            stepFive.addProperty("type", "command");
            stepFive.addProperty("action", CommandActionsGame.GameCommand.INCREASE_SYSTEMS_POWER.getGameBinding());
            _gameCommandHandler.handleGrokResponse(stepFive);
            log.info("Diverting power step 5");
            Thread.sleep(DELAY);

            log.info("Power distribution complete");
        } catch (InterruptedException e) {
            //ok
        }
    }
}
