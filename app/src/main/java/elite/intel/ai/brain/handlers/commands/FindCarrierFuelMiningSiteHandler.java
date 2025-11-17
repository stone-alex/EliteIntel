package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.stellarobjects.ReserveLevel;
import elite.intel.ai.search.spansh.stellarobjects.StellarObjectSearch;
import elite.intel.ai.search.spansh.stellarobjects.StellarObjectSearchResultDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GetNumberFromParam;

import java.util.Optional;


public class FindCarrierFuelMiningSiteHandler extends CommandOperator implements CommandHandler {

    final private GameController gameController;

    public FindCarrierFuelMiningSiteHandler(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.getNumberFromParam(params, 1000);
        EventBusManager.publish(new AiVoxResponseEvent("Searching for Carrier Fuel Mining Site withing " + range.intValue() + "... Stand by..."));

        StellarObjectSearchResultDto tritiumLocations = StellarObjectSearch.getInstance()
                .findRings(
                        "Tritium",
                        ReserveLevel.PRISTINE,
                        PlayerSession.getInstance().getGalacticCoordinates(),
                        range.intValue()
                );

        if (tritiumLocations == null || tritiumLocations.getResults().isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No Tritium locations found."));
            return;
        }

        Optional<StellarObjectSearchResultDto.Result> result = tritiumLocations.getResults().stream().findFirst();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);
        routePlotter.plotRoute(result.get().getSystemName());
    }
}
