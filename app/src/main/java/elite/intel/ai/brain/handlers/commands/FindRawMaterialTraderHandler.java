package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.search.spansh.station.traderandbroker.TraderType;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.util.json.GetNumberFromParam;

public class FindRawMaterialTraderHandler extends CommandOperator implements CommandHandler {

    public static final int DEFAULT_RANGE = 250;
    private final GameController gameController;

    public FindRawMaterialTraderHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.gameController = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if(status.isInSrv() || status.isInMainShip()) {
            Number range = GetNumberFromParam.getNumberFromParam(params, DEFAULT_RANGE);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching for " + TraderType.RAW.getType() + " material traders... Stand by..."));
            TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
            RoutePlotter routePlotter = new RoutePlotter(this.gameController);
            routePlotter.plotRoute(search.location(TraderType.RAW, null, range.intValue()));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Route can only be plotted in SRV or Main Ship."));
        }
    }
}
