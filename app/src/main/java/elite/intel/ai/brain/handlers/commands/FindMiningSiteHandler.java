package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.fuzzyMaterialSearch;

public class FindMiningSiteHandler extends CommandOperator implements CommandHandler {

    private GameController controller;

    public FindMiningSiteHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if(!status.isInMainShip()){
            EventBusManager.publish(new AiVoxResponseEvent("Please board your ship."));
            return;
        }

        JsonElement key = params.get("key");
        if (key == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Did not catch the material name."));
        }

        String material =
                capitalizeWords(
                        fuzzyMaterialSearch(
                                key.getAsString(), 3
                        )
                );
        LocationManager locationManager = LocationManager.getInstance();
        LocationDao.Coordinates coordinates = locationManager.getGalacticCoordinates();
    }
}
