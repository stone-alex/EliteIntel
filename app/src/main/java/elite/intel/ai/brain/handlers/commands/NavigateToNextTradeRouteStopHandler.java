package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.DestinationDto;
import elite.intel.search.spansh.traderoute.TradeCommodityInfo;
import elite.intel.search.spansh.traderoute.TradeRouteStationInfo;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class NavigateToNextTradeRouteStopHandler extends CommandOperator implements CommandHandler {

    private GameController gameController;

    public NavigateToNextTradeRouteStopHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.gameController = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        final RoutePlotter routePlotter = new RoutePlotter(gameController);
        final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
        final DestinationReminderManager reminderManager = DestinationReminderManager.getInstance();

        if (!tradeRouteManager.hasRoute()) {
            EventBusManager.publish(new AiVoxResponseEvent("No trade route found."));
            return;
        }

        TradeRouteManager.TradeRouteLegTuple<TradeCommodityInfo, TradeRouteStationInfo, String> nextStop = tradeRouteManager.getNextStop();
        TradeRouteStationInfo tradePort = nextStop.getTradeRouteStationInfo();
        routePlotter.plotRoute(tradePort.getSystem());


        reminderManager.setDestination(
                    new Reminder(
                        nextStop.getCommodityInfo(), nextStop.getTradeRouteStationInfo(), nextStop.getCommodityName()
                    ).toJson()
        );
    }

    record Reminder(TradeCommodityInfo commodityInfo, TradeRouteStationInfo tadePortInfo, String commodityName) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
