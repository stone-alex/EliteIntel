package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MiningAnnouncementEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.capitalizeWords;

public class RemoveMiningTargetHandler implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        JsonElement key = params.get("key");
        if (key == null) {
            EventBusManager.publish(new MiningAnnouncementEvent("Sorry did not catch the material name."));
            return;
        }
        String target = capitalizeWords(
                FuzzySearch.fuzzyMaterialNameSearch(
                        key.getAsString(), 3
                )
        );

        if (target == null || target.isEmpty()) {
            EventBusManager.publish(new MiningAnnouncementEvent("Unable to find " + key.getAsString() + " material in the database."));
            return;
        } else {
            playerSession.removeMiningTarget(target);
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(target + " removed from mining targets."));

    }
}
