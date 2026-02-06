package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.session.PlayerSession;

import java.util.List;

public class PlotRouteToNextTradeStopHandler extends CommandOperator implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final GameController gameController;

    public PlotRouteToNextTradeStopHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.gameController = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        final RoutePlotter routePlotter = new RoutePlotter(gameController);
        final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
        final ReminderManager reminderManager = ReminderManager.getInstance();

        if (!tradeRouteManager.hasRoute()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No trade route found."));
            return;
        }

        GameEvents.CargoEvent shipCargo = playerSession.getShipCargo();
        boolean cargoLoaded = shipCargo.getCount() > 0;

        TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> nextStop = tradeRouteManager.getNextStop();
        if(nextStop == null){
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No more stops to visit."));
            return;
        }

        StringBuilder sb = new StringBuilder();
        List<TradeCommodity> commodities = nextStop.getTradeStopDto().getCommodities();
        if (!cargoLoaded) {
            String sourceSystem = nextStop.getTradeStopDto().getSourceSystem();
            String sourceStation = nextStop.getTradeStopDto().getSourceStation();
            sb.append("We are heading to ").append(sourceSystem).append(", ").append(sourceStation).append(" there we will pick up ");
            commodities.forEach(commodity -> sb.append(commodity.getName()).append(", "));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
            routePlotter.plotRoute(sourceSystem);
        } else {
            String destinationSystem = nextStop.getTradeStopDto().getDestinationSystem();
            String destinationStation = nextStop.getTradeStopDto().getDestinationStation();
            sb.append("We are heading to ").append(destinationSystem).append(", ").append(destinationStation).append(" to sell the freight.");
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
            routePlotter.plotRoute(destinationSystem);
        }

        reminderManager.setDestination(sb.toString());
    }
}
