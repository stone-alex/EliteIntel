package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.BrainTreeManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.util.StringUtls.capitalizeWords;

public class FindBrainTreesHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final BrainTreeManager brainTreeManager = BrainTreeManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    public FindBrainTreesHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        if (brainTreeManager.getCount() == 0) {
            //NOTE:
            // We can get local data from Spansh, this is a fast all.
            // Brain Tree's do not disappear form the galaxy, but new locations could be added.
            brainTreeManager.retrieveFromSpansh();
        }

        JsonElement key = params.get("key");
        if (key == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Did not catch the material name."));
            return;
        }

        String material =
                capitalizeWords(
                        FuzzySearch.fuzzyMaterialNameSearch(
                                key.getAsString(), 8
                        )
                );

        LocationDao.Coordinates coordinates = locationManager.getGalacticCoordinates();
        StellarObjectSearchResultDto.Result result = brainTreeManager.findNearestWithMaterial(material, coordinates.x(), coordinates.y(), coordinates.z());
        if (result == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Brain Tree locations found."));
        } else {
            EventBusManager.publish(
                    new MissionCriticalAnnouncementEvent(
                            "Found nearest Brain Tree at " + result.getSystemName()
                                    + ". Located " + result.getDistance() + " light years away."
                                    + " Head to planet " + result.getBodyName()
                    )
            );
            RoutePlotter plotter = new RoutePlotter(this.controller);
            plotter.plotRoute(result.getSystemName());
            ReminderManager.getInstance()
                    .setReminder(
                            "Head to " + result.getSystemName() + " planet " + result.getBodyName()
                    );
        }
    }

    record DataDto(String starSystem, String planetName) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
