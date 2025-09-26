package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.gameapi.journal.events.LocationEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class LocationSubscriber {

    @Subscribe
    public void onLocationEvent(LocationEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto dto = playerSession.getCurrentLocation();
        dto.setX(event.getStarPos()[0]);
        dto.setY(event.getStarPos()[1]);
        dto.setZ(event.getStarPos()[2]);
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
        dto.setSecurity(event.getSystemSecurityLocalised());
        if(event.getStationFaction() != null) dto.setStationFaction(event.getStationFaction().getName());

        dto.setTrafficDto(EdsmApiClient.searchTraffic(event.getStarSystem()));
        dto.setDeathsDto(EdsmApiClient.searchDeaths(event.getStarSystem()));

        playerSession.saveCurrentLocation(dto);
    }
}
