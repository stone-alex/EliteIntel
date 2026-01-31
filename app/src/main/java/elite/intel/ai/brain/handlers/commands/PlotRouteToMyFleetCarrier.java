package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

public class PlotRouteToMyFleetCarrier extends CommandOperator implements CommandHandler {

    private final GameController controller;

    public PlotRouteToMyFleetCarrier(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.controller = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        Status status = Status.getInstance();
        if (status.isInSrv() || status.isInMainShip()) {
            PlayerSession playerSession = PlayerSession.getInstance();
            boolean hasFleetCarrier = playerSession.getCarrierData() != null;
            boolean hasHomeSystem = playerSession.getHomeSystem() != null;

            String destination;
            if (hasFleetCarrier) {
                destination = playerSession.getLastKnownCarrierLocation();
            } else if (hasHomeSystem) {
                destination = playerSession.getHomeSystem().getStarName();
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("No home system available for route plotting."));
                return;
            }

            if (destination != null && !destination.isEmpty()) {
                RoutePlotter plotter = new RoutePlotter(this.controller);
                plotter.plotRoute(destination);
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Carrier location not available."));
            }
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Route can only be plotted in SRV or Main Ship."));
        }
    }
}
