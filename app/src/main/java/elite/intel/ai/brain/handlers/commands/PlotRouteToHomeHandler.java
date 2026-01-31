package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

public class PlotRouteToHomeHandler extends CommandOperator implements CommandHandler {

    private final GameController commandHandler;
    private final PlayerSession playerSession = PlayerSession.getInstance();

    public PlotRouteToHomeHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        Status status = Status.getInstance();
        if(status.isInSrv() || status.isInMainShip()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Plotting route to home system..."));
            LocationDto location = playerSession.getHomeSystem();
            if (location.getBodyId() == -1) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Home system is not set. We are homeless!"));
                return;
            }
            RoutePlotter plotter = new RoutePlotter(commandHandler);
            plotter.plotRoute(location.getStarName());
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Route can only be plotted in SRV or Main Ship."));
        }
    }
}
