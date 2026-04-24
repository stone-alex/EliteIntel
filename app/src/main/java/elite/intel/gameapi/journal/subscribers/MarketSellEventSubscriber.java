package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.MarketSellEvent;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MarketSellEventSubscriber {

    private static final int DEBOUNCE_MS = 2000;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<MarketSellEvent> pending = new ArrayList<>();
    private ScheduledFuture<?> pendingFlush;

    @Subscribe
    public void onMarketSellEvent(MarketSellEvent event) {
        TradeRouteManager.getInstance().deleteForMarketId(event.getMarketID());

        synchronized (pending) {
            pending.add(event);
            if (pendingFlush != null) pendingFlush.cancel(false);
            pendingFlush = scheduler.schedule(this::flush, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void flush() {
        synchronized (pending) {
            if (pending.isEmpty()) return;

            if (pending.size() == 1) {
                MarketSellEvent e = pending.getFirst();
                EventBusManager.publish(new AiVoxResponseEvent("Sold " + e.getCount() + " units of " + e.getType() + " for " + e.getTotalSale() + " credits."));
            } else {
                long total = pending.stream().mapToLong(MarketSellEvent::getTotalSale).sum();
                EventBusManager.publish(new AiVoxResponseEvent("Sold " + pending.size() + " commodities for " + total + " credits total."));
            }
            pending.clear();

            final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
            final PlayerSession playerSession = PlayerSession.getInstance();
            final ReminderManager reminderManager = ReminderManager.getInstance();

            TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> nextStop = tradeRouteManager.getNextStop();

            if (nextStop != null) {
                String sourceSystem = nextStop.getTradeStopDto().getSourceSystem();
                String sourceStation = nextStop.getTradeStopDto().getSourceStation();
                String destinationSystem = nextStop.getTradeStopDto().getDestinationSystem();
                String destinationStation = nextStop.getTradeStopDto().getDestinationStation();

                StringBuilder sb = new StringBuilder();
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
                reminderManager.setReminder(sb.toString(), destinationSystem);
            } else {
                reminderManager.clear();
            }

            MonetizeRouteManager.getInstance().clear();
        }
    }


    public record Reminder(Integer legNumber, TradeStopDto stopInfo,
                           List<TradeCommodity> commodities) implements ToJsonConvertible {
        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
