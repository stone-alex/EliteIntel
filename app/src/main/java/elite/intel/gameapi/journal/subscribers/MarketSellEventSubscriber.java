package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.MarketSellEvent;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class MarketSellEventSubscriber {

    @Subscribe public void onMarketSellEvent(MarketSellEvent event) {
        final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
        final PlayerSession playerSession = PlayerSession.getInstance();
        final ReminderManager reminderManager = ReminderManager.getInstance();

        tradeRouteManager.deleteForMarketId(event.getMarketID());
        EventBusManager.publish(new AiVoxResponseEvent("Sold " + event.getCount() + " units of " + event.getType() + " for " + event.getTotalSale() + " credits."));

        TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> nextStop = tradeRouteManager.getNextStop();

        StringBuilder sb = new StringBuilder();
        if (nextStop != null) {
            String sourceSystem = nextStop.getTradeStopDto().getSourceSystem();
            String sourceStation = nextStop.getTradeStopDto().getSourceStation();
            String destinationSystem = nextStop.getTradeStopDto().getDestinationSystem();
            String destinationStation = nextStop.getTradeStopDto().getDestinationStation();

            if (playerSession.getPrimaryStarName().equalsIgnoreCase(sourceSystem)) {
                sb.append(" Buy ");
                nextStop.getTradeStopDto().getCommodities().forEach(commodity -> sb.append(commodity.getName()).append(", "));
                sb.append(" Sell at ").append(destinationSystem).append(", ").append(destinationStation).append(" port.");
            } else {
                sb.append(" Head to ").append(sourceSystem).append(", ").append(sourceStation).append(" and buy ");
                nextStop.getTradeStopDto().getCommodities().forEach(commodity -> sb.append(commodity.getName()).append(", "));
                sb.append(" Sell at ").append(destinationSystem).append(", ").append(destinationStation).append(" port.");
            }



            EventBusManager.publish(new AiVoxResponseEvent(sb.toString()));
            reminderManager.setDestination(
                    sb.toString()
            );
        } else {
            reminderManager.clear();
        }

        MonetizeRouteManager.getInstance().clear();
    }


    public record Reminder(Integer legNumber, TradeStopDto stopInfo, List<TradeCommodity> commodities) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
