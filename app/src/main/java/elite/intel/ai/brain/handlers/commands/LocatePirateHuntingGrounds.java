package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.PirateMissionDataManager.PirateMissionTuple;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.missions.pirates.PirateMassacreMissionSearch;

import java.util.List;

import static elite.intel.util.StringUtls.getIntSafely;

public class LocatePirateHuntingGrounds implements CommandHandler {

    /**
     * No Recon
     * Will query the local database first. If nothing is found will call external API
     */
    @Override public void handle(String action, JsonObject params, String responseText) {
        Integer range = getIntSafely(params.get("key").getAsString()) == null || params.isEmpty() ? 100 : params.get("key").getAsInt();
        PirateMassacreMissionSearch missionSearch = PirateMassacreMissionSearch.getInstance();
        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> huntingGrounds = missionSearch.findHuntingSpotsInRange(range, false);
        StringBuilder sb = new StringBuilder();
        if (huntingGrounds.isEmpty()) {
            sb.append("No mission providers found.");
        } else {
            sb.append("Found " + huntingGrounds.size() + " mission provider");
            if(huntingGrounds.size() > 1) sb.append("s");
            sb.append(". Ask me to plot route to nearest hunting ground or mission provider. ");
            boolean reconRequired = huntingGrounds.stream().anyMatch(data -> data.getTarget().getTargetFaction() == null);
            if (reconRequired) sb.append(" Reconnaissance is required.");
        }
        EventBusManager.publish(new AiVoxResponseEvent(sb.toString()));
    }
}
