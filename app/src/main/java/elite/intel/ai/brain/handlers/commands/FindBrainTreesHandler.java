package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.BrainTreeManager;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.fuzzyMaterialSearch;

public class FindBrainTreesHandler extends CommandOperator implements CommandHandler {

    private GameController controller;

    public FindBrainTreesHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        BrainTreeManager brainTreeManager = BrainTreeManager.getInstance();
        LocationManager locationManager = LocationManager.getInstance();
        int totalInDb = brainTreeManager.getCount();
        if (totalInDb == 0) {
            //NOTE:
            // We can get local data from Spansh, this is a fast all.
            // Brain Tree's do not disappear form the galaxy, but new locations could be added.
            brainTreeManager.retrieveFromSpansh();
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

        LocationDao.Coordinates coordinates = locationManager.getGalacticCoordinates();
        StellarObjectSearchResultDto.Result result = brainTreeManager.findNearestWithMaterial(material, coordinates.x(), coordinates.y(), coordinates.z());
        if (result == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No Brain Tree locations found."));
        } else {
            EventBusManager.publish(
                    new AiVoxResponseEvent(
                            "Found nearest Brain Tree at " + result.getSystemName()
                                    + ". Located " + result.getDistance() + " light years away."
                                    + " Head to planet " + result.getBodyName()
                    )
            );
            RoutePlotter plotter = new RoutePlotter(this.controller);
            plotter.plotRoute(result.getSystemName());
            DestinationReminderManager.getInstance()
                    .setDestination(
                            new DataDto(result.getSystemName(), result.getBodyName()).toJson()
                    );
        }
    }

    record DataDto(String starSystem, String planetName) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
