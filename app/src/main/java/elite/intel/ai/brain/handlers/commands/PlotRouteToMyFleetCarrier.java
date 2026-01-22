package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
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
        if(status.isInSrv() || status.isInMainShip()) {
            PlayerSession playerSession = PlayerSession.getInstance();
            String carrierLocation = playerSession.getLastKnownCarrierLocation();

            if (carrierLocation != null && !carrierLocation.isEmpty()) {
                RoutePlotter plotter = new RoutePlotter(this.controller);
                plotter.plotRoute(carrierLocation);
            } else {
                EventBusManager.publish(new AiVoxResponseEvent("Carrier location not available."));
            }
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Route can only be plotted in SRV or Main Ship."));
        }
    }
}
