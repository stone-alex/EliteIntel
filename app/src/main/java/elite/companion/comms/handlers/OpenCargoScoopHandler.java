package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.robot.VoiceCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import elite.companion.comms.CommandAction;

public class OpenCargoScoopHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(OpenCargoScoopHandler.class);
    private final VoiceCommandHandler voiceCommandHandler;

    public OpenCargoScoopHandler(VoiceCommandHandler voiceCommandHandler) {
        this.voiceCommandHandler = voiceCommandHandler;
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "command");
        //json.addProperty("action", CommandAction.OPEN_CARGO_SCOOP.getAction());
        json.addProperty("response_text", responseText);
        voiceCommandHandler.handleGrokResponse(json);
        log.info("Handled open cargo scoop command");
    }
}