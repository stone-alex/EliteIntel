package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;

public class PlotRouteToMyFleetCarrier extends CustomCommandOperator implements CommandHandler {

    private final GameHandler commandHandler;

    public PlotRouteToMyFleetCarrier(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.commandHandler = commandHandler;
    }

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();

        if (carrierData == null) {
            EventBusManager.publish(new VoiceProcessEvent("No carrier data found."));
            return;
        }

        String carrierLocation = carrierData.getLocation();
        if(carrierLocation != null && !carrierLocation.isEmpty()) {
            RoutePlotter plotter = new RoutePlotter(this.commandHandler);
            plotter.plotRoute(carrierLocation);
        } else {
            EventBusManager.publish(new VoiceProcessEvent("Carrier location not available."));
        }
    }
}
