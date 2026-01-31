package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public void handle(String action, JsonObject params, String responseText) {
        PlayerSession session = PlayerSession.getInstance();
        session.setMiningAnnouncementOn(true);
        String target = params.get("key").getAsString();

        if (target == null && target.isEmpty()) {
            log.info("no mining target set");
        } else {

            session.addMiningTarget(target);
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Mining target set to " + target + ". Mining announcement enabled."));
    }
}