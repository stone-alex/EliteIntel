package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlotRouteToHomeHandler extends CustomCommandOperator implements CommandHandler {


    private static final Logger log = LogManager.getLogger(PlotRouteToHomeHandler.class);
    private final GameHandler commandHandler;

    public PlotRouteToHomeHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        RoutePlotter plotter = new RoutePlotter(commandHandler);
        LocationDto homeSystem = playerSession.getHomeSystem();
        plotter.plotRoute(homeSystem.getStarName());
    }
}
