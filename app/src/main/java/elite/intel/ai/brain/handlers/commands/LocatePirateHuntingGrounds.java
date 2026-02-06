package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateHuntingGroundsDao.HuntingGround;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.HuntingGroundManager.PirateMissionTuple;
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
        int range = params.get("key") == null || getIntSafely(params.get("key").getAsString()) == null || params.isEmpty() ? 100 : params.get("key").getAsInt();

        PirateMassacreMissionSearch missionSearch = PirateMassacreMissionSearch.getInstance();
        List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> huntingGrounds = missionSearch.findHuntingSpotsInRange(range);
        


        StringBuilder sb = new StringBuilder();
        if (huntingGrounds.isEmpty()) {
            sb.append("No mission providers found.");
        } else {
            sb.append("Found " + huntingGrounds.size() + " mission provider");
            if(huntingGrounds.size() > 1) sb.append("s");
            sb.append(". Ask me to recon target system. ");
            boolean reconRequired = huntingGrounds.stream().anyMatch(data -> data.getTarget().getTargetFaction() == null);
            if (reconRequired) sb.append(" Reconnaissance is required.");
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
    }
}
