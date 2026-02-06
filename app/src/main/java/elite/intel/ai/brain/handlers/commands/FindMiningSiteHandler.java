package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.stellarobjects.ReserveLevel;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearch;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.session.Status;

import java.util.Optional;

import static elite.intel.util.StringUtls.capitalizeWords;

public class FindMiningSiteHandler extends CommandOperator implements CommandHandler {

    public static final int MAX_DEFAULT_RANGE = 1000;
    private final GameController controller;

    public FindMiningSiteHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (!status.isInMainShip()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Please board your ship."));
            return;
        }

        JsonElement mat = params.get("key");
        JsonElement distance = params.get("max_distance");
        if (mat == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Did not catch the material name."));
            return;
        }

        String material =
                capitalizeWords(
                        FuzzySearch.fuzzyMaterialNameSearch(
                                mat.getAsString(), 8
                        )
                );

        StellarObjectSearchResultDto miningLocations = StellarObjectSearch.getInstance()
                .findRings(
                        material,
                        ReserveLevel.PRISTINE,
                        LocationManager.getInstance().getGalacticCoordinates(),
                        distance == null ? MAX_DEFAULT_RANGE : distance.getAsInt()
                );

        if (miningLocations == null || miningLocations.getResults().isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Mining sites found."));
            return;
        }

        Optional<StellarObjectSearchResultDto.Result> result = miningLocations.getResults().stream().findFirst();
        if (result.isPresent()) {
            RoutePlotter routePlotter = new RoutePlotter(this.controller);
            routePlotter.plotRoute(result.get().getSystemName());
            String reminder = "Found nearest mining location in " + result.get().getSystemName() + " system head to planet " + result.get().getBodyName();
            ReminderManager.getInstance().setDestination(reminder);
            EventBusManager.publish(new AiVoxResponseEvent(reminder));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No mining sites found within range."));
        }
    }
}
