package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.comms.GameCommandMapping;
import elite.companion.comms.VoiceGenerator;
import elite.companion.robot.VoiceCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployLandingGearHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(DeployLandingGearHandler.class);
    private final VoiceCommandHandler voiceCommandHandler;

    public DeployLandingGearHandler(VoiceCommandHandler voiceCommandHandler) {
        this.voiceCommandHandler = voiceCommandHandler;
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        String gameBinding = GameCommandMapping.GameCommand.LANDING_GEAR_TOGGLE.getGameBinding();
        if (gameBinding == null) {
            log.error("No game binding found for command: {}", params.get("action").getAsString());
            VoiceGenerator.getInstance().speak("No key binding found for deploying landing gear.");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "command");
        json.addProperty("action", gameBinding);
        json.addProperty("response_text", responseText);
        voiceCommandHandler.handleGrokResponse(json);
        log.info("Handled deploy landing gear command with binding: {}", gameBinding);
    }
}