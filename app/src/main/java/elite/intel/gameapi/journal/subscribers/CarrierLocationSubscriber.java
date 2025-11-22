package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.spansh.carrierroute.CarrierJump;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.Map;
import java.util.Set;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.PRIMARY_STAR;

public class CarrierLocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            FleetCarrierRouteManager route = FleetCarrierRouteManager.getInstance();
            CarrierDataDto carrierData = playerSession.getCarrierData();
            carrierData.setStarName(event.getStarSystem());
            FleetCarrierRouteManager.getInstance().removeLeg(event.getStarSystem());
            Map<Integer, CarrierJump> fleetCarrierRoute = route.getFleetCarrierRoute();
            boolean routeEntryFount = false;

            Status status = Status.getInstance();
            if(!status.isDocked() && playerSession.getMarkedId() == event.getCarrierID()) {
                LocationManager locationData = LocationManager.getInstance();
                LocationDto prima = locationData.getPrimaryStarAtCurrentLocation();
                carrierData.setX(prima.getX());
                carrierData.setY(prima.getY());
                carrierData.setZ(prima.getZ());
                carrierData.setStarName(event.getStarSystem());
                playerSession.setCarrierData(carrierData);
            } else {
                for (Map.Entry<Integer, CarrierJump> entry : fleetCarrierRoute.entrySet()) {
                    if (entry.getValue().getSystemName().equals(event.getStarSystem())) {
                        routeEntryFount = true;
                        carrierData.setX(entry.getValue().getX());
                        carrierData.setY(entry.getValue().getY());
                        carrierData.setZ(entry.getValue().getZ());
                        carrierData.setStarName(event.getStarSystem());
                        playerSession.setLastKnownCarrierLocation(event.getStarSystem());
                        playerSession.setCarrierData(carrierData);
                        break;
                    }
                }
            }
            if(!routeEntryFount) {
                String carrierName = carrierData.getCarrierName();
                if(carrierName == null) {return;}
                LocationManager locationData = LocationManager.getInstance();
                Map<Long, LocationDto> locations = locationData.findByPrimaryStar(event.getStarSystem());
                if (locations != null || !locations.isEmpty()) {
                    for (LocationDto historyLocation : locations.values()) {
                        Set<FssSignalDto> detectedSignals = historyLocation.getDetectedSignals();
                        for (FssSignalDto signal : detectedSignals) {
                            boolean matchingCarrierName = signal.getSignalName().toLowerCase().contains(carrierName.toLowerCase());
                            boolean isPrimaryStar = historyLocation.getLocationType().equals(PRIMARY_STAR);
                            if (matchingCarrierName && isPrimaryStar) {
                                carrierData.setX(historyLocation.getX());
                                carrierData.setY(historyLocation.getY());
                                carrierData.setZ(historyLocation.getZ());
                                playerSession.setCarrierData(carrierData);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
