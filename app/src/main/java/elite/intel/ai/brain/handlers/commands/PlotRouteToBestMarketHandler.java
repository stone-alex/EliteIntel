package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.spansh.market.StationMarketDto;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

import java.util.Comparator;
import java.util.List;

public class PlotRouteToBestMarketHandler extends CommandOperator implements CommandHandler {


    private final GameController commandHandler;

    public PlotRouteToBestMarketHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarketDto> markets = playerSession.getMarkets();
        if (markets != null && !markets.isEmpty()) {
            StationMarketDto bestMarket = markets.stream()
                    .min(Comparator.comparingDouble(StationMarketDto::getSellPrice))
                    .orElse(null);

            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(bestMarket.systemName());
            DestinationReminderManager reminder = DestinationReminderManager.getInstance();
            reminder.setDestination(bestMarket.toJson());
            EventBusManager.publish(new AiVoxResponseEvent("Route plotted. Head to " + bestMarket.stationName() + " when you get there."));
        }
    }
}
