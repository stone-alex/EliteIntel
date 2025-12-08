package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.search.spansh.client.SpanshClient;
import elite.intel.search.spansh.nearest.NearestKnownLocationSearchClient;
import elite.intel.search.spansh.stellarobjects.ReserveLevel;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearch;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.NavigationUtils;
import elite.intel.util.json.GetNumberFromParam;

import java.util.Optional;


public class FindCarrierFuelMiningSiteHandler extends CommandOperator implements CommandHandler {

    final private GameController gameController;

    public FindCarrierFuelMiningSiteHandler(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (status.isInSrv() || status.isInMainShip()) {
            Number range = GetNumberFromParam.getNumberFromParam(params, 1000);
            EventBusManager.publish(new AiVoxResponseEvent("Searching for Carrier Fuel Mining Site withing " + range.intValue() + " light years... Stand by..."));

            ShipRouteManager shipRouteManager = ShipRouteManager.getInstance();
            shipRouteManager.clearRoute();
            LocationDao.Coordinates coordinates = LocationManager.getInstance().getGalacticCoordinates();
            StellarObjectSearchResultDto tritiumLocations = StellarObjectSearch.getInstance()
                    .findRings(
                            "Tritium",
                            ReserveLevel.PRISTINE,
                            coordinates,
                            range.intValue()
                    );

            if (tritiumLocations == null || tritiumLocations.getResults().isEmpty()) {
                EventBusManager.publish(new AiVoxResponseEvent("No Tritium locations found."));
                return;
            }

            Optional<StellarObjectSearchResultDto.Result> result = tritiumLocations.getResults().stream().findFirst();
            double distance = NavigationUtils.calculateGalacticDistance(result.get().getX(), result.get().getY(), result.get().getZ(), coordinates.x(), coordinates.y(), coordinates.z());
            if(distance > range.intValue()){
                EventBusManager.publish(new AiVoxResponseEvent("No Tritium locations found within range."));
                return;
            }


            DestinationReminderManager reminderManager = DestinationReminderManager.getInstance();
            reminderManager.setDestination(result.get().toJson());
            RoutePlotter routePlotter = new RoutePlotter(this.gameController);
            routePlotter.plotRoute(result.get().getSystemName());
            EventBusManager.publish(new AiVoxResponseEvent("Head to " + result.get().getSystemName() + " star system."));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("You must be in SRV or Main Ship to use this command."));
        }
    }
}
