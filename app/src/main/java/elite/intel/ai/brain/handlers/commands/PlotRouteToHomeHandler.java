package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;

public class PlotRouteToHomeHandler extends CommandOperator implements CommandHandler {

    private final GameController commandHandler;

    public PlotRouteToHomeHandler(GameController commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new AiVoxResponseEvent("Plotting route to home system..."));
        LocationManager locations = LocationManager.getInstance();
        LocationDto location =locations.getHomeSystem();
        if(location.getBodyId() == -1){
            EventBusManager.publish(new AiVoxResponseEvent("Home system is not set. We are homeless!"));
            return;
        }
        RoutePlotter plotter = new RoutePlotter(commandHandler);
        plotter.plotRoute(location.getStarName());
    }
}
