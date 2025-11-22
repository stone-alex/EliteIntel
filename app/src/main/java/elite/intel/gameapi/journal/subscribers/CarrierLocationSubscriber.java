package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.StationsDto;
import elite.intel.ai.search.spansh.carrierroute.CarrierJump;
import elite.intel.ai.search.spansh.client.SpanshClient;
import elite.intel.ai.search.spansh.stellarobjects.StellarObjectSearchClient;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

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


            // via carrier route
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

            if (!routeEntryFount) {
                // try via EDSM
                StarSystemDto starSystemDto = EdsmApiClient.searchStarSystem(event.getStarSystem(), 1);
                StarSystemDto.Coords coords = starSystemDto.getCoords();
                if(coords.getX() > 0 && coords.getY() > 0 && coords.getZ() > 0) {
                    carrierData.setX(coords.getX());
                    carrierData.setY(coords.getY());
                    carrierData.setZ(coords.getZ());
                    playerSession.setCarrierData(carrierData);
                } else {
                    // try via saved locations
                    LocationManager locationData = LocationManager.getInstance();
                    LocationDto location = locationData.findPrimaryStar(event.getStarSystem());
                    carrierData.setX(location.getX());
                    carrierData.setY(location.getY());
                    carrierData.setZ(location.getZ());
                    playerSession.setCarrierData(carrierData);
                }
            }
        }
    }

}
