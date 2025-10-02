package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
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
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setMarketID(event.getMarketID());
        currentLocation.setStationName(event.getStationName());
        currentLocation.setStationEconomy(event.getStationEconomyLocalised());
        currentLocation.setStationServices(event.getStationServices());
        currentLocation.setStationType(event.getStationType());
        currentLocation.setStationGovernment(event.getStationGovernmentLocalised());
        if("FleetCarrier".equalsIgnoreCase(event.getStationType())) {
            currentLocation.setLocationType(FLEET_CARRIER);
            PlayerSession.GalacticCoordinates coordinates = playerSession.getGalacticCoordinates();
            if(coordinates != null) {
                CarrierDataDto carrierData = playerSession.getCarrierData();
                carrierData.setX(coordinates.x());
                carrierData.setY(coordinates.y());
                carrierData.setZ(coordinates.z());
                carrierData.setLocation(event.getStarSystem());
                playerSession.setCarrierData(carrierData);
            }
        } else {
            currentLocation.setLocationType(STATION);
        }

        if(event.getStationFaction()  != null) currentLocation.setStationFaction(event.getStationFaction().getName());

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
            EventBusManager.publish(new VocalisationRequestEvent("Data available for: " + availableData + "."));
        }

        playerSession.saveCurrentLocation(currentLocation);
    }
}
