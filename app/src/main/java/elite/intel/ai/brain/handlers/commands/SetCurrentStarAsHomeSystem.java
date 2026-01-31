package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class SetCurrentStarAsHomeSystem implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        LocationDao.Coordinates coordinates = LocationManager.getInstance().getGalacticCoordinates();
        if (coordinates == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Galactic coordinates are not available."));
            return;
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Setting " + coordinates.primaryStar() + " as home system."));
        playerSession.setHomeSystem(locationManager.findByLocationData(playerSession.getLocationData()));
    }
}
