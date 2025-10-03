package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;

import java.util.Map;
import java.util.Objects;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.FLEET_CARRIER;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();

        double[] starPos = event.getStarPos();
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setLastKnownCarrierLocation(starSystem);

        CarrierDataDto carrierData = playerSession.getCarrierData();
        playerSession.setCarrierDepartureTime(null);

        if(FLEET_CARRIER.equals(playerSession.getCurrentLocation().getLocationType())){
            playerSession.saveLocation(toLocationDto(event));
            playerSession.clearLocations();
        }

        if(carrierData != null && starPos[0] > 0) {
            carrierData.setX(starPos[0]);
            carrierData.setY(starPos[1]);
            carrierData.setZ(starPos[2]);
            carrierData.setLocation(starSystem);
            playerSession.setCarrierData(carrierData);
        }

        EventBusManager.publish(new SensorDataEvent("Carrier Location: " + event.getStarSystem()));
    }

    private LocationDto toLocationDto(CarrierJumpEvent event) {
        LocationDto location;
        LocationHistory locationHistory = LocationHistory.getInstance(event.getStarSystem());
        Map<Long, LocationDto> locations = locationHistory.getLocations();
        if (locations == null || locations.isEmpty()) {
            location = new LocationDto(event.getBodyId());
            return fillInWhatWeCan(event, location);
        } else {
            location = locations.get((long) event.getBodyId());
            return fillInWhatWeCan(event, Objects.requireNonNullElseGet(location, () -> new LocationDto(event.getBodyId())));
        }
    }

    private LocationDto fillInWhatWeCan(CarrierJumpEvent event, LocationDto location) {
        location.setBodyId(event.getBodyId());
        location.setAllegiance(event.getSystemAllegiance());
        location.setStarName(event.getStarSystem());
        location.setX(event.getStarPos()[0]);
        location.setY(event.getStarPos()[1]);
        location.setZ(event.getStarPos()[2]);
        location.setStationGovernment(event.getSystemGovernmentLocalised());
        location.setPlanetName(event.getBody());
        return location;
    }
}