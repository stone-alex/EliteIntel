package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.util.StringUtls;

public class SetFleetCarrierFuelReserveHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        JsonElement key = params.get("key");
        if (key == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Invalid fuel reserve value received."));
        }
        Integer reserve = StringUtls.getIntSafely(key.getAsString().replace(",", ""));
        if(reserve == null){
            EventBusManager.publish(new AiVoxResponseEvent("Invalid fuel reserve value received."));
            return;
        }
        FleetCarrierManager fleetCarrierManager = FleetCarrierManager.getInstance();
        CarrierDataDto dto = fleetCarrierManager.get();
        dto.setFuelReserve(reserve);
        fleetCarrierManager.save(dto);
        EventBusManager.publish(new AiVoxResponseEvent("Fuel reserve set to " + reserve));
    }
}
