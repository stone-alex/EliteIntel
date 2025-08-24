package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.comms.GameCommandMapping;
import elite.companion.comms.VoiceGenerator;
import elite.companion.robot.VoiceCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetPowerToWeaponsHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPowerToWeaponsHandler.class);
    public static final int DELAY = 25;
    private final VoiceCommandHandler voiceCommandHandler;

    public SetPowerToWeaponsHandler(VoiceCommandHandler voiceCommandHandler) {
        this.voiceCommandHandler = voiceCommandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {

            JsonObject resetPowerJson = new JsonObject();
            resetPowerJson.addProperty("type", "command");
            resetPowerJson.addProperty("action", GameCommandMapping.GameCommand.RESET_POWER_DISTRIBUTION.getGameBinding());
            voiceCommandHandler.handleGrokResponse(resetPowerJson);
            Thread.sleep(DELAY);


            JsonObject stepOne = new JsonObject();
            stepOne.addProperty("type", "command");
            stepOne.addProperty("action", GameCommandMapping.GameCommand.INCREASE_WEAPONS_POWER.getGameBinding());
            voiceCommandHandler.handleGrokResponse(stepOne);
            Thread.sleep(DELAY);

            JsonObject stepTwo = new JsonObject();
            stepTwo.addProperty("type", "command");
            stepTwo.addProperty("action", GameCommandMapping.GameCommand.INCREASE_ENGINES_POWER.getGameBinding());
            voiceCommandHandler.handleGrokResponse(stepTwo);
            Thread.sleep(DELAY);

            JsonObject stepThree = new JsonObject();
            stepThree.addProperty("type", "command");
            stepThree.addProperty("action", GameCommandMapping.GameCommand.INCREASE_WEAPONS_POWER.getGameBinding());
            voiceCommandHandler.handleGrokResponse(stepThree);
            Thread.sleep(DELAY);

            JsonObject stepFour = new JsonObject();
            stepFour.addProperty("type", "command");
            stepFour.addProperty("action", GameCommandMapping.GameCommand.INCREASE_ENGINES_POWER.getGameBinding());
            voiceCommandHandler.handleGrokResponse(stepFour);
            Thread.sleep(DELAY);

            JsonObject stepFive = new JsonObject();
            stepFive.addProperty("type", "command");
            stepFive.addProperty("action", GameCommandMapping.GameCommand.INCREASE_WEAPONS_POWER.getGameBinding());
            voiceCommandHandler.handleGrokResponse(stepFive);
            Thread.sleep(DELAY);

            log.info("Diverting power to weapons");
            VoiceGenerator.getInstance().speak(responseText);
        } catch (InterruptedException e) {
            //ok
        }
    }
}
