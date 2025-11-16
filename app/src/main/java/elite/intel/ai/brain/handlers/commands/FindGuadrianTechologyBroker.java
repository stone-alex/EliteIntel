package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.station.traderandbroker.BrokerType;
import elite.intel.gameapi.EventBusManager;

public class FindGuadrianTechologyBroker extends CommandOperator implements CommandHandler {

    private GameController gameController;

    public FindGuadrianTechologyBroker(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number distance = params.get("key") == null ? 250 : params.get("key").getAsNumber();
        EventBusManager.publish(new AiVoxResponseEvent("Searching for " + BrokerType.GUARDIAN.getType() + " techology broker... Stand by..."));
        TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);
        routePlotter.plotRoute(search.location(null, BrokerType.HUMAN, distance));
    }
}
