package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.DockedEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.session.PlayerSession;

import java.util.List;
import java.util.Locale;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.FLEET_CARRIER;
import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.STATION;

public class DockedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onDockedEvent(DockedEvent event) {

        /// this is a workaround
        /*
         * Docked Event does not have a system address or body id. However, it has market id and market data.
         * This means we have to grab the location where we dropped from supercruise and set these numbers here.
         * */
        LocationDto location = locationManager.findByLocationData(playerSession.getLocationData());
        //LocationDto location = locationManager.findByMarketId(event.getMarketID());

        location.setMarketID(event.getMarketID());
        location.setStationEconomy(event.getStationEconomyLocalised());
        location.setStationServices(event.getStationServices());
        location.setStationType(event.getStationType());
        location.setStationGovernment(event.getStationGovernmentLocalised());
        location.setStarName(event.getStarSystem());
        location.setStationName(event.getStationName());
        location.setPlanetName(null);
        location.setPlanetShortName(null);


        MarketDto edsmMarketDto = EdsmApiClient.searchMarket(event.getMarketID(), null, null, 0);
        location.setMarket(edsmMarketDto);
        location.setStationName(edsmMarketDto.getData().getStationName());
        LocationDto.LocationType locationType = LocationDto.determineType(event.getStationType().toLowerCase(Locale.ROOT), false);
        if (FLEET_CARRIER == locationType) {
            location.setLocationType(FLEET_CARRIER);
            LocationDao.Coordinates coordinates = LocationManager.getInstance().getGalacticCoordinates();
            if (coordinates != null) {
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

        if (event.getStationFaction() != null) location.setStationFaction(event.getStationFaction().getName());

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
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Market data available."));
        }
        locationManager.save(location);
    }
}
