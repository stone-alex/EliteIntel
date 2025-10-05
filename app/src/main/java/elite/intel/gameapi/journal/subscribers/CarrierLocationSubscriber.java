package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;
import elite.intel.util.AdjustRoute;

import java.util.Map;
import java.util.Set;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.FLEET_CARRIER;
import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.PRIMARY_STAR;

public class CarrierLocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            CarrierDataDto carrierData = playerSession.getCarrierData();

            AdjustRoute.adjustFleetCarrierRoute(event.getStarSystem());

            String carrierName = carrierData.getCarrierName();
            LocationHistory history = LocationHistory.getInstance(event.getStarSystem());
            Map<Long, LocationDto> locations = history.getLocations();
            if (locations != null || !locations.isEmpty()) {
                for(LocationDto historyLocation : locations.values()){
                    Set<FssSignalDto> detectedSignals = historyLocation.getDetectedSignals();
                    for(FssSignalDto signal : detectedSignals){
                        boolean matchingCarrierName = signal.getSignalName().toLowerCase().contains(carrierName.toLowerCase());
                        boolean isPrimaryStar = historyLocation.getLocationType().equals(PRIMARY_STAR);
                        if(matchingCarrierName && isPrimaryStar){
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
