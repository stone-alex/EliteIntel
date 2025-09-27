package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.session.PlayerSession;

public class PlotRouteToMyFleetCarrier extends CustomCommandOperator implements CommandHandler {

    private final GameHandler commandHandler;

    public PlotRouteToMyFleetCarrier(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();


        String carrierLocation = String.valueOf(playerSession.get(PlayerSession.CARRIER_LOCATION));
        if(carrierLocation != null && !carrierLocation.isEmpty()) {
            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(carrierLocation);
        } else {
            EventBusManager.publish(new VoiceProcessEvent("Carrier location not available."));
        }
    }
}
