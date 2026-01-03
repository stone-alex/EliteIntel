package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class SetCurrentStarAsHomeSystem implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        LocationDao.Coordinates coordinates = LocationManager.getInstance().getGalacticCoordinates();
        if (coordinates == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Galactic coordinates are not available."));
            return;
        }
        EventBusManager.publish(new AiVoxResponseEvent("Setting " + coordinates.primaryStar() + " as home system."));
        PlayerSession.getInstance().setHomeSystem();
    }
}
