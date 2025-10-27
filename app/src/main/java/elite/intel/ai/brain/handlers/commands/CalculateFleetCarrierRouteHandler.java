package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.ai.search.spansh.carrier.CarrierRouteCriteria;
import elite.intel.ai.search.spansh.carrier.SpanshCarrierRouter;
import elite.intel.ai.search.spansh.nearest.NearestKnownLocationSearch;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.ClipboardUtils;

import java.util.Map;

public class CalculateFleetCarrierRouteHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        SpanshCarrierRouter client = new SpanshCarrierRouter();

        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        int fuelSupply = carrierData.getFuelSupply();
        Integer tritiumInReserve = carrierData.getCommodity().get("tritium");
        if (tritiumInReserve != null && tritiumInReserve > 0) {
            fuelSupply = fuelSupply + tritiumInReserve;
        }


        int cargoCapacity = carrierData.getCargoCapacity();
        int cargoSpaceUsed = carrierData.getCargoSpaceUsed();
        String destination = ClipboardUtils.getClipboardText();
        EventBusManager.publish(new AiVoxResponseEvent("Accessing Spansh... Stand by..."));

        if(carrierData.getX() == 0 && carrierData.getY() == 0 && carrierData.getZ() == 0) {
            EventBusManager.publish(new AiVoxResponseEvent("Fleet Carrier location not available."));
            return;
        }

        LocationDto nearestStartingPoint = NearestKnownLocationSearch.findNearest(carrierData.getX(), carrierData.getY(), carrierData.getZ());

        if (destination == null || nearestStartingPoint == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No destination or nearest known location found."));
            return;
        }

        CarrierRouteCriteria carrierRouteCriteria = new CarrierRouteCriteria(
                nearestStartingPoint.getStarName(),
                destination,
                cargoCapacity,
                cargoSpaceUsed,
                fuelSupply
        );

        Map<Integer, CarrierJump> route = client.calculateRoute(carrierRouteCriteria, fuelSupply);
        playerSession.setFleetCarrierRoute(route);

        int fuelRequired = route.values().stream().mapToInt(CarrierJump::getFuelUsed).sum();
        int numJumps = route.size();

        if (numJumps == 0) {
            EventBusManager.publish(new AiVoxResponseEvent("Unable to plot route to " + destination + ". Data is not available in Spansh. Try another star system near by."));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent(
                    "Calculated Fleet Carrier route to " + destination + ". " +
                            numJumps + " jumps, with a total of " +
                            fuelRequired + " tons of fuel required. When you are ready - open the Galaxy Map from Fleet Carrier panel, select the text field and ask me to enter next carrier jump location.")
            );
        }

    }
}
