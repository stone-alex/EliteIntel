package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.stellarobjects.ReserveLevel;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearch;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.session.Status;

import java.util.Optional;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.fuzzyMaterialSearch;

public class FindMiningSiteHandler extends CommandOperator implements CommandHandler {

    public static final int MAX_DEFAULT_RANGE = 1000;
    private GameController controller;

    public FindMiningSiteHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (!status.isInMainShip()) {
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

        StellarObjectSearchResultDto tritiumLocations = StellarObjectSearch.getInstance()
                .findRings(
                        material,
                        ReserveLevel.PRISTINE,
                        LocationManager.getInstance().getGalacticCoordinates(),
                        MAX_DEFAULT_RANGE
                );

        if (tritiumLocations == null || tritiumLocations.getResults().isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No Tritium locations found."));
            return;
        }

        Optional<StellarObjectSearchResultDto.Result> result = tritiumLocations.getResults().stream().findFirst();
        RoutePlotter routePlotter = new RoutePlotter(this.controller);
        routePlotter.plotRoute(result.get().getSystemName());
        DestinationReminderManager.getInstance().setDestination(result.get().toJson());
        EventBusManager.publish(new AiVoxResponseEvent("Found nearest " + material + " in " + result.get().getSystemName()+" system on planet "+result.get().getBodyName()));
    }
}
