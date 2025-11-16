package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.station.traderandbroker.TraderType;
import elite.intel.gameapi.EventBusManager;

public class FindEncodedMaterialTraderHandler extends CommandOperator implements CommandHandler {

    private final GameController gameController;

    public FindEncodedMaterialTraderHandler(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }


    @Override public void handle(String action, JsonObject params, String responseText) {
        Number distance = params.get("key") == null ? 250 : params.get("key").getAsNumber();
        EventBusManager.publish(new AiVoxResponseEvent("Searching for " + TraderType.ENCODED.getType() + " material traders... Stand by..."));
        TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);
        routePlotter.plotRoute(search.location(TraderType.ENCODED, null, distance));
    }
}
