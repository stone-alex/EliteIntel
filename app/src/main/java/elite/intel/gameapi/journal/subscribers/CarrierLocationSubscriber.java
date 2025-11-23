package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.StarSystemDto;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class CarrierLocationSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            FleetCarrierRouteManager route = FleetCarrierRouteManager.getInstance();
            CarrierDataDto carrierData = playerSession.getCarrierData();
            carrierData.setStarName(event.getStarSystem());

            CarrierJump currentCompletedJump = route.findByPrimaryStar(event.getStarSystem());
            boolean routeEntryFound = false;
            if (currentCompletedJump != null) {
                carrierData.setX(currentCompletedJump.getX());
                carrierData.setY(currentCompletedJump.getY());
                carrierData.setZ(currentCompletedJump.getZ());
                playerSession.setCarrierData(carrierData);
                routeEntryFound = true;
            }

            route.removeLeg(event.getStarSystem());

            if (!routeEntryFound) {
                // try via EDSM
                StarSystemDto starSystemDto = EdsmApiClient.searchStarSystem(event.getStarSystem(), 1);
                StarSystemDto.Coords coords = starSystemDto.getCoords();
                if (coords != null && coords.getX() > 0 && coords.getY() > 0 && coords.getZ() > 0) {
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
