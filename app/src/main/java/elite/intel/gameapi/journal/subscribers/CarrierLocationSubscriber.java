package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;

import java.util.Map;
import java.util.Set;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.FLEET_CARRIER;

public class CarrierLocationSubscriber {

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            CarrierDataDto carrierData = playerSession.getCarrierData();

            String carrierName = carrierData.getCarrierName();
            LocationHistory history = LocationHistory.getInstance(event.getStarSystem());
            Map<Long, LocationDto> locations = history.getLocations();
            if (locations != null || !locations.isEmpty()) {
                for(LocationDto historyLocation : locations.values()){
                    Set<FssSignalDto> detectedSignals = historyLocation.getDetectedSignals();
                    for(FssSignalDto signal : detectedSignals){
                        boolean matchingCarrierName = signal.getSignalName().toLowerCase().contains(carrierName.toLowerCase());
                        boolean isCarrierLocationType = historyLocation.getLocationType().equals(FLEET_CARRIER);
                        if(matchingCarrierName && isCarrierLocationType){
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
