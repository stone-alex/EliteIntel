package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class PlotRouteToMyFleetCarrier extends CustomCommandOperator implements CommandHandler {

    private final GameController commandHandler;

    public PlotRouteToMyFleetCarrier(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();


        String carrierLocation = playerSession.getLastKnownCarrierLocation();
        if(carrierLocation != null && !carrierLocation.isEmpty()) {
            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(carrierLocation);
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Carrier location not available."));
        }
    }
}
