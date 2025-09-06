package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericGameController implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(GenericGameController.class);
    private final GameCommandHandler _gameCommandHandler;
    private final String gameBinding;

    public GenericGameController(GameCommandHandler gameCommandHandler, String gameBinding) {
        this._gameCommandHandler = gameCommandHandler;
        this.gameBinding = gameBinding;
    }

    @Override public void handle(JsonObject params, String responseText) {
        if (gameBinding == null) {
            log.error("No game binding found for command: {}", params.get("action").getAsString());
            EventBusManager.publish(new VoiceProcessEvent("Generic Game controller No key binding found for " + gameBinding));
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "command");
        json.addProperty("action", gameBinding);
        json.addProperty("response_text", responseText);
        _gameCommandHandler.handleGrokResponse(json);
        log.info(gameBinding + " command handled");
    }

}
