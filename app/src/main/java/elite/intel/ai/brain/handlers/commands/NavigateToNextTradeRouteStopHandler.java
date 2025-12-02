package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

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

        TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> nextStop = tradeRouteManager.getNextStop();
        routePlotter.plotRoute(nextStop.getTradeStopDto().getSourceSystem());

        reminderManager.setDestination(
                new Reminder(
                        nextStop.getLegNumber(), nextStop.getTradeStopDto(), nextStop.getTradeStopDto().getCommodities()
                ).toJson()
        );
    }

    public record Reminder(Integer legNumber, TradeStopDto stopInfo, List<TradeCommodity> commodities) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
