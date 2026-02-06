package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class ConfirmHuntingGroundHandler implements CommandHandler {

    private final HuntingGroundManager missionDataManager = HuntingGroundManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();


    @Override public void handle(String action, JsonObject params, String responseText) {
        LocationDto location = locationManager.findByLocationData(playerSession.getLocationData());
        missionDataManager.confirmTargetReconResourceSite(location.getStarName());
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("hunting ground confirmed"));
    }
}
