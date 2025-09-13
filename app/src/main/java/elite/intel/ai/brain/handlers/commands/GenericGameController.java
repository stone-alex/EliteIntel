package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GenericGameController class acts as a mediator between the game command input and
 * the GameCommandHandler, translating commands and passing necessary information for
 * execution. It specifically processes JSON-based parameters and forwards them to the
 * GameCommandHandler after associating them with a defined game binding.
 * <p>
 * This class implements the CommandHandler interface, ensuring any incoming commands
 * are handled following the specified contract. It also manages error scenarios where
 * no valid game binding is found, logging the issue and publishing an error event.
 * <p>
 * Key responsibilities:
 * - Interpret and map incoming command parameters using a pre-defined game binding.
 * - Forward structured command data to the GameCommandHandler for actual processing.
 * - Most of the game controller commands go through this class.
 * - Log handling activities and provide feedback when no valid binding is defined.
 */
public class GenericGameController implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(GenericGameController.class);
    private final GameHandler gameHandler;
    private final String gameBinding;

    public GenericGameController(GameHandler gameHandler, String gameBinding) {
        this.gameHandler = gameHandler;
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
        gameHandler.processAiCommand(json);
        log.info(gameBinding + " command handled");
    }

}
