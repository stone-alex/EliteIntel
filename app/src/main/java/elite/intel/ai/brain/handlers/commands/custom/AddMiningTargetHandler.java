package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

/**
 * The SetMiningTargetHandler class processes the command to set a mining target for the player session.
 * It implements the CommandHandler interface to provide handling logic for the specified command.
 * <p>
 * The handler retrieves the mining target parameter from the provided JSON object.
 * If the target is valid, it updates the player's session with the new mining target value.
 * If the provided parameter is missing or empty, it logs an informational message indicating no target was set.
 */
public class AddMiningTargetHandler implements CommandHandler {
    private static final Logger log = LogManager.getLogger(AddMiningTargetHandler.class);

    @Override
    public void handle(JsonObject params, String responseText) {
        PlayerSession session = PlayerSession.getInstance();
        session.setMiningAnnouncementOn(true);
        JsonElement jsonElement = params.get("material");

        if (jsonElement == null || jsonElement.getAsString().isEmpty()) {
            log.info("no mining target set");
        } else {
            String miningTarget = jsonElement.getAsJsonPrimitive().getAsString().replace("\"", "");
            session.addMiningTarget(miningTarget);
        }
        EventBusManager.publish(new AiVoxResponseEvent("Mining target set. Mining announcement enabled."));
    }
}