package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.ai.search.spansh.station.traderandbroker.BrokerType;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GetNumberFromParam;

public class FindGuadrianTechnologyBroker extends CommandOperator implements CommandHandler {

    public static final int DEFAULT_RANGE = 250;
    private GameController gameController;

    public FindGuadrianTechnologyBroker(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.getNumberFromParam(params, DEFAULT_RANGE);
        EventBusManager.publish(new AiVoxResponseEvent("Searching for " + BrokerType.GUARDIAN.getType() + " technology broker... Stand by..."));
        TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);
        routePlotter.plotRoute(search.location(null, BrokerType.HUMAN, range));
    }
}
