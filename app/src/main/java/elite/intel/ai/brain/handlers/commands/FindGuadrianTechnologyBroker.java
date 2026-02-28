package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.search.spansh.station.traderandbroker.BrokerType;
import elite.intel.util.json.GetNumberFromParam;

public class FindGuadrianTechnologyBroker extends CommandOperator implements CommandHandler {

    public static final int DEFAULT_RANGE = 250;
    private final GameController gameController;

    public FindGuadrianTechnologyBroker(GameController gameController) {
        super(gameController.getMonitor(), gameController.getExecutor());
        this.gameController = gameController;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.extractRangeParameter(params, DEFAULT_RANGE);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching for " + BrokerType.GUARDIAN.getType() + " technology broker. Stand by."));
        TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
        RoutePlotter routePlotter = new RoutePlotter(this.gameController);

        String location = search.location(null, BrokerType.HUMAN, range);
        if (location != null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Guardian tech broker available"));
        } else {
            routePlotter.plotRoute(location);
        }
        routePlotter.plotRoute(location);
    }
}
