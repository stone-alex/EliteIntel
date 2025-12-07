package elite.intel.util;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.search.spansh.carrierroute.CarrierRouteCriteria;
import elite.intel.search.spansh.carrierroute.SpanshCarrierRouteClient;
import elite.intel.search.spansh.nearest.NearestKnownLocationSearchClient;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class FleetCarrierRouteCalculator {

    public static void calculate() {

        SpanshCarrierRouteClient client = new SpanshCarrierRouteClient();
        PlayerSession playerSession = PlayerSession.getInstance();
        FleetCarrierRouteManager routeManager = FleetCarrierRouteManager.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();

        int fuelSupply = carrierData.getFuelLevel();
        Integer tritiumInReserve = carrierData.getCommodity().get("tritium");
        if (tritiumInReserve != null && tritiumInReserve > 0) {
            fuelSupply = fuelSupply + tritiumInReserve;
        }


        int cargoCapacity = carrierData.getCargoCapacity();
        int cargoSpaceUsed = carrierData.getCargoSpaceUsed();
        String destination = ClipboardUtils.getClipboardText();
        String existingFinalDestination = routeManager.getFinalDestination();

        if(!destination.equalsIgnoreCase(existingFinalDestination) && !existingFinalDestination.isEmpty()){
            // re-calculating
            destination = existingFinalDestination;
        }

        EventBusManager.publish(new AiVoxResponseEvent("Accessing Spansh... Stand by..."));

        if(carrierData.getX() == 0 && carrierData.getY() == 0 && carrierData.getZ() == 0) {
            EventBusManager.publish(new AiVoxResponseEvent("Fleet Carrier location not available."));
            return;
        }

        LocationDto nearestStartingPoint = NearestKnownLocationSearchClient.findNearest(carrierData.getX(), carrierData.getY(), carrierData.getZ());

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
        routeManager.setFleetCarrierRoute(route);

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