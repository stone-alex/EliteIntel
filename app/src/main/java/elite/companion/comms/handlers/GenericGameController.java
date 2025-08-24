package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.comms.VoiceGenerator;
import elite.companion.robot.VoiceCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericGameController implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(GenericGameController.class);
    private final VoiceCommandHandler voiceCommandHandler;
    private final String gameBinding;

    public GenericGameController(VoiceCommandHandler voiceCommandHandler, String gameBinding) {
        this.voiceCommandHandler = voiceCommandHandler;
        this.gameBinding = gameBinding;
    }

    @Override public void handle(JsonObject params, String responseText) {
        if (gameBinding == null) {
            log.error("No game binding found for command: {}", params.get("action").getAsString());
            VoiceGenerator.getInstance().speak("No key binding found for " + gameBinding);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "command");
        json.addProperty("action", gameBinding);
        json.addProperty("response_text", responseText);
        voiceCommandHandler.handleGrokResponse(json);
        log.info(gameBinding + " command handled");
    }

}
