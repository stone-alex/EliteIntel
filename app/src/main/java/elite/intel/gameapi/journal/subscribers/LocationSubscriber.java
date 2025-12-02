package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.LocationEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class LocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onLocationEvent(LocationEvent event) {

        LocationDto dto = findLocation(event);

        dto.setX(event.getStarPos()[0]);
        dto.setY(event.getStarPos()[1]);
        dto.setZ(event.getStarPos()[2]);
        dto.setBodyId(event.getBodyID());
        dto.setStarName(event.getStarSystem());
        dto.setPlanetName(event.getBody());
        dto.setAllegiance(event.getSystemAllegiance());
        dto.setBodyType(event.getBodyType());
        dto.setControllingPower(event.getControllingPower());
        dto.setGovernment(event.getSystemGovernmentLocalised());
        dto.setPopulation(event.getPopulation());
        dto.setSecurity(event.getSystemSecurity());
        dto.setStationAllegiance(event.getStationAllegiance());
        dto.setStationEconomy(event.getStationEconomyLocalised());
        dto.setStationGovernment(event.getStationGovernmentLocalised());
        dto.setStationServices(event.getStationServices());
        dto.setStarName(event.getStationName());
        dto.setStationType(event.getStationType());
        dto.setDistance(event.getDistFromStarLS());
        dto.setEconomy(event.getSystemEconomyLocalised());
        dto.setSecondEconomy(event.getSystemSecondEconomyLocalised());

        //TODO: Need a util to figure what type of location this is.
        dto.setStationType(event.getStationType());
        if ("FleetCarrier".equalsIgnoreCase(event.getStationType())) {
            dto.setLocationType(LocationDto.LocationType.FLEET_CARRIER);
        }

        dto.setPopulation(event.getPopulation());
        dto.setPowerplayState(event.getPowerplayState());
        dto.setPowerplayStateControlProgress(event.getPowerplayStateControlProgress());
        dto.setPowerplayStateReinforcement(event.getPowerplayStateReinforcement());
        dto.setPowerplayStateUndermining(event.getPowerplayStateUndermining());
        dto.setSecurity(event.getSystemSecurityLocalised());
        dto.setStationName(event.getStationName());

        if(event.getStationFaction() != null) dto.setStationFaction(event.getStationFaction().getName());

        dto.setTrafficDto(EdsmApiClient.searchTraffic(event.getStarSystem()));
        dto.setDeathsDto(EdsmApiClient.searchDeaths(event.getStarSystem()));

        if(dto.getStarName() != null && dto.getStarName().length() > 0) {
            //have to check for star name (primary star of the system). Sometimes the star name is empty.
            //do not save locations without star name.
            playerSession.saveLocation(dto);
        }
    }

    private LocationDto findLocation(LocationEvent event) {
        LocationDto dto;
        LocationManager locationData = LocationManager.getInstance();
        Map<Long, LocationDto> locations = locationData.findByPrimaryStar(event.getStarSystem());

        if (locations == null || locations.isEmpty()) {
            dto = playerSession.getLocation(event.getBodyID(), event.getStarSystem());
            if(dto == null) {
                dto = playerSession.getCurrentLocation();
            }
        } else {
            dto = locations.get((long) event.getBodyID());
            if (dto == null) {
                dto = playerSession.getLocation(event.getBodyID(), event.getStarSystem());
                if(dto == null) {
                    dto = playerSession.getCurrentLocation();
                }
            }
        }
        return dto;
    }
}
