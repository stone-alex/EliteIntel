package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;
import elite.intel.util.AdjustRoute;

import java.util.Map;
import java.util.Set;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.PRIMARY_STAR;

public class CarrierLocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            CarrierDataDto carrierData = playerSession.getCarrierData();
            carrierData.setStarName(event.getStarSystem());
            AdjustRoute.adjustFleetCarrierRoute(event.getStarSystem());
            Map<Integer, CarrierJump> fleetCarrierRoute = playerSession.getFleetCarrierRoute();
            boolean routeEntryFount = false;
            for(Map.Entry<Integer, CarrierJump> entry : fleetCarrierRoute.entrySet()) {
                if(entry.getValue().getSystemName().equals(event.getStarSystem())) {
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

            if(!routeEntryFount) {
                String carrierName = carrierData.getCarrierName();
                LocationHistory history = LocationHistory.getInstance(event.getStarSystem());
                Map<Long, LocationDto> locations = history.getLocations();
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
