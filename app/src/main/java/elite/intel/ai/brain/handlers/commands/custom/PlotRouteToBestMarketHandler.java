package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

import java.util.Comparator;
import java.util.List;

public class PlotRouteToBestMarketHandler extends CustomCommandOperator implements CommandHandler {


    private final GameController commandHandler;

    public PlotRouteToBestMarketHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarket> markets = playerSession.getMarkets();
        if (markets != null && !markets.isEmpty()) {
            StationMarket bestMarket = markets.stream()
                    .min(Comparator.comparingDouble(StationMarket::getSellPrice))
                    .orElse(null);

            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(bestMarket.systemName());
            playerSession.setTargetMarketStation(bestMarket);
            EventBusManager.publish(new AiVoxResponseEvent("Route plotted. Head to " + bestMarket.stationName() + " when you get there."));
        }
    }
}
