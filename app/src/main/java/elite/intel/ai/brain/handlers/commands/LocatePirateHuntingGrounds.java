package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.PirateMissionDataManager.PirateMissionTuple;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.missions.pirates.PirateMassacreMissionSearch;

import java.util.List;

public class LocatePirateHuntingGrounds implements CommandHandler {

    /**
     * No Recon
     * Will query the local database first. If nothing is found will call external API
     */
    @Override public void handle(String action, JsonObject params, String responseText) {
        Integer range = params.get("key") == null || params.isEmpty() ? 100 : params.get("key").getAsInt();
        PirateMassacreMissionSearch missionSearch = PirateMassacreMissionSearch.getInstance();

        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> huntingGrounds = missionSearch.findHuntingSpotsInRange(range, false);

        StringBuilder sb = new StringBuilder();
        if (huntingGrounds.isEmpty()) {
            sb.append("No mission providers found.");
        } else {
            sb.append("Missions found: ");
            huntingGrounds.forEach(data -> {

                sb.append(". Target star system " + data.getTarget().getStarSystem());
                if (data.getTarget().getTargetFaction() == null) {
                    sb.append(". Reconnaissance required ");
                }

                data.getMissionProvider().forEach(provider -> {
                            sb.append(". Mission Provider System " + provider.getStarSystem());
                            if (provider.getMissionProviderFaction() == null) {
                                sb.append(". Reconnaissance required ");
                            }
                        }
                );
            });
        }
        EventBusManager.publish(new AiVoxResponseEvent(sb.toString()));
    }
}
