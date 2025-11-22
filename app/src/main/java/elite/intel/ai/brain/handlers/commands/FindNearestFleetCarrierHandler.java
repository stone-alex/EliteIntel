package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.spansh.findcarrier.CarrierAccess;
import elite.intel.search.spansh.findcarrier.FleetCarrierSearch;
import elite.intel.search.spansh.findcarrier.FleetCarrierSearchResultsDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimeUtils;
import elite.intel.util.json.GetNumberFromParam;

public class FindNearestFleetCarrierHandler extends CommandOperator implements CommandHandler {

    private final GameController gameController;

    public FindNearestFleetCarrierHandler(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.getNumberFromParam(params, 500);
        EventBusManager.publish(new AiVoxResponseEvent("Searching for nearest fleet carrier with public access within " + range.intValue() + " light years... Stand by..."));

        PlayerSession playerSession = PlayerSession.getInstance();
        FleetCarrierSearchResultsDto fleetCarriers = FleetCarrierSearch.getInstance()
                .findFleetCarrier(
                        range.intValue(),
                        CarrierAccess.ALL,
                        playerSession.getGalacticCoordinates()
                );

        String playerCarrierCallSign = null;
        CarrierDataDto carrierData = playerSession.getCarrierData();
        if (carrierData != null) {
            playerCarrierCallSign = carrierData.getCallSign();
        }

        final String finalPlayerCarrierCallSign = playerCarrierCallSign;
        fleetCarriers.getResults().stream()
                .filter(carrier -> !carrier.getCallSign().equals(finalPlayerCarrierCallSign))
                .findFirst()
                .ifPresentOrElse(
                        result -> {
                            RoutePlotter routePlotter = new RoutePlotter(this.gameController);
                            String dateAsString = result.getUpdatedAt();
                            String timeAgo = TimeUtils.transformToYMDHtimeAgo(dateAsString, TimeUtils.LOCAL_DATE_TIME);
                            EventBusManager.publish(new AiVoxResponseEvent("Found fleet carrier " + result.getCallSign() + " at " + result.getSystemName() + ". Data last updated " + timeAgo));
                            routePlotter.plotRoute(result.getSystemName());
                        },
                        () -> EventBusManager.publish(new AiVoxResponseEvent("No fleet carriers found within range."))
                );
    }
}
