package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.session.PlayerSession;

import java.util.List;

public class PlotRouteToBestMarketHandler extends CustomCommandOperator implements CommandHandler {


    private final GameHandler commandHandler;

    public PlotRouteToBestMarketHandler(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarket> markets = playerSession.getMarkets();
        if (markets != null && !markets.isEmpty()) {
            StationMarket bestMarket = markets.stream()
                    .min((m1, m2) -> Double.compare(m1.getSellPrice(), m2.getSellPrice()))
                    .orElse(null);

            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(bestMarket.systemName());
            playerSession.clearMarkets();
        }
    }
}
