package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.DockedEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.FLEET_CARRIER;
import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.STATION;

public class DockedSubscriber {

    @Subscribe
    public void onDockedEvent(DockedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto location = playerSession.getLocation(event.getMarketID(), event.getStarSystem());

        location.setMarketID(event.getMarketID());
        location.setStationName(event.getStationName());
        location.setStationEconomy(event.getStationEconomyLocalised());
        location.setStationServices(event.getStationServices());
        location.setStationType(event.getStationType());
        location.setStationGovernment(event.getStationGovernmentLocalised());
        location.setStarName(event.getStarSystem());
        location.setMarketID(event.getMarketID());
        location.setStationName(event.getStationName());
        location.setPlanetName(null);
        location.setPlanetShortName(null);
        MarketDto marketDto = EdsmApiClient.searchMarket(event.getMarketID(), null, null);
        if(marketDto != null) {
            location.setMarket(marketDto);
            location.setStationName(marketDto.getData().getStationName());
        }
        if("FleetCarrier".equalsIgnoreCase(event.getStationType())) {
            location.setLocationType(FLEET_CARRIER);
            LocationDao.Coordinates coordinates = LocationManager.getInstance().getGalacticCoordinates();
            if(coordinates != null) {
                CarrierDataDto carrierData = playerSession.getCarrierData();
                carrierData.setX(coordinates.x());
                carrierData.setY(coordinates.y());
                carrierData.setZ(coordinates.z());
                carrierData.setStarName(event.getStarSystem());
                playerSession.setCarrierData(carrierData);
            }
        } else {
            location.setLocationType(STATION);
        }

        if(event.getStationFaction()  != null) location.setStationFaction(event.getStationFaction().getName());

        StringBuilder sb = new StringBuilder();
        List<String> stationServices = event.getStationServices();
        if (stationServices != null && !stationServices.isEmpty()) {
            sb.append("Services: ");
            for (String service : stationServices) {
                sb.append(service);
                sb.append(", ");
            }
            sb.append(".");
        }

        DockedEvent.LandingPads landingPads = event.getLandingPads();
        if (landingPads != null) {
            sb.append(" Landing Pads:");
            sb.append(" Large: ").append(landingPads.getLarge()).append(", ");
            sb.append(" Medium: ").append(landingPads.getMedium()).append(", ");
            sb.append(" Small: ").append(landingPads.getSmall()).append(".");
        }

        String availableData = LocalServicesData.setLocalServicesData(event.getMarketID());
        if (!availableData.isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Data available for: " + availableData + "."));
        }
        playerSession.saveLocation(location);
    }
}
