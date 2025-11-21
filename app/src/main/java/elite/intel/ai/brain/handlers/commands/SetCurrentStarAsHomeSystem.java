package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.db.managers.LocationManager;
import elite.intel.session.PlayerSession;

public class SetCurrentStarAsHomeSystem implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        LocationManager locations = LocationManager.getInstance();
        locations.setAsHomeSystem(PlayerSession.getInstance().getPrimaryStarName());
    }
}
