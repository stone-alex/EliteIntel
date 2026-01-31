package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MiningAnnouncementEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.StringUtls.capitalizeWords;

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
        JsonElement key = params.get("key");
        if(key == null){
            EventBusManager.publish(new MiningAnnouncementEvent("Sorry did not catch the material name."));
            return;
        }
        String target = capitalizeWords(
                        FuzzySearch.fuzzyMaterialNameSearch(
                                key.getAsString(), 8
                        )
                );

        if (target == null || target.isEmpty()) {
            EventBusManager.publish(new MiningAnnouncementEvent("Unable to find this material in the database."));
            return;
        } else {
            session.addMiningTarget(target);
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Mining target set to " + target + ". Mining announcement enabled."));
    }
}