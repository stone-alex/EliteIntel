package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.TradeCommodity;
import elite.intel.session.PlayerSession;

import java.util.List;

public class PlotRouteToNextTradeStopHandler extends CommandOperator implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
    private final ReminderManager reminderManager = ReminderManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final GameController gameController;

    public PlotRouteToNextTradeStopHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.gameController = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        final RoutePlotter routePlotter = new RoutePlotter(gameController);
        final LocationDto location = locationManager.findByLocationData(playerSession.getLocationData());
        if (!tradeRouteManager.hasRoute()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No trade route found."));
            return;
        }

        GameEvents.CargoEvent shipCargo = playerSession.getShipCargo();
        boolean cargoLoaded = shipCargo.getCount() > 0;

        TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> nextStop = tradeRouteManager.getNextStop();
        if (nextStop == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No more stops to visit."));
            return;
        }

        String sourceSystem = nextStop.getTradeStopDto().getSourceSystem();
        String sourceStation = nextStop.getTradeStopDto().getSourceStation();
        String destinationSystem = nextStop.getTradeStopDto().getDestinationSystem();
        String destinationStation = nextStop.getTradeStopDto().getDestinationStation();

        StringBuilder sb = new StringBuilder();
        List<TradeCommodity> commodities = nextStop.getTradeStopDto().getCommodities();
        if (!cargoLoaded) {
            boolean notInSourceSystem = !location.getStarName().equalsIgnoreCase(sourceSystem);
            boolean notAtTheSourceStation = !location.getStationName().equalsIgnoreCase(sourceStation);

            if (notInSourceSystem) {
                sb.append(" Travel to ").append(sourceSystem).append(", ");
                sb.append( " visit " ).append(sourceStation).append(" and pick up ");
            } else if(notAtTheSourceStation) {
                sb.append(" In this star system visit ");
                sb.append(sourceStation).append(" and pick up ");
            } else {
                sb.append(" At this station buy ");
            }
            commodities.forEach(commodity -> sb.append(commodity.getName()).append(", "));
            sb.append(" and sell at ").append(destinationSystem).append(" star system, ").append(destinationStation).append(" port.");

            if (notInSourceSystem) {
                routePlotter.plotRoute(sourceSystem);
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
        } else {

            boolean notInDestinationSystem = !location.getStarName().equalsIgnoreCase(destinationSystem);
            boolean notAtTheDestinationStation = !location.getStationName().equalsIgnoreCase(destinationStation);

            if (notInDestinationSystem) {
                sb.append(" Travel to ").append(destinationSystem).append(", ").append(destinationStation).append(" to sell the freight.");
                routePlotter.plotRoute(destinationSystem);
            } else if( notAtTheDestinationStation) {
                sb.append(" Head to ").append(destinationStation).append(" to sell the freight.");
            } else {
                sb.append(" Sell freight here. ");
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
        }

        reminderManager.setReminder(sb.toString());
    }
}
