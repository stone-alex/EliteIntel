package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.HomeSystem;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlotRouteToHomeHandler extends CommandOperator implements CommandHandler {


    private static final Logger log = LogManager.getLogger(PlotRouteToHomeHandler.class);
    private final GameController commandHandler;

    public PlotRouteToHomeHandler(GameController commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        HomeSystem homeSystem = HomeSystem.getInstance();
        RoutePlotter plotter = new RoutePlotter(commandHandler);
        LocationDto location = homeSystem.getHomeSystem();
        plotter.plotRoute(location.getStarName());
    }
}
