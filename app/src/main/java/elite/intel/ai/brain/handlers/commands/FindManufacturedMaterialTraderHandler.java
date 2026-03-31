package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.TradersAndBrokersSearch;
import elite.intel.search.spansh.station.traderandbroker.TraderType;
import elite.intel.session.Status;
import elite.intel.util.json.GetNumberFromParam;

public class FindManufacturedMaterialTraderHandler implements CommandHandler {

    public static final int DEFAULT_RANGE = 250;


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if(status.isInSrv() || status.isInMainShip()) {
            Number range = GetNumberFromParam.extractRangeParameter(params, DEFAULT_RANGE);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching for " + TraderType.MANUFACTURED.getType() + " material traders. Stand by."));
            TradersAndBrokersSearch search = TradersAndBrokersSearch.getInstance();
            RoutePlotter routePlotter = new RoutePlotter();
            routePlotter.plotRoute(search.location(TraderType.MANUFACTURED, null, range));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Route can only be plotted in SRV or Main Ship."));
        }
    }
}
