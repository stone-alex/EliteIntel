package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.TouchdownEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;


public class DistanceFromShipTracker {

    private static final Logger log = LogManager.getLogger(DistanceFromShipTracker.class);
    private boolean isInDonutArea = false;

    @Subscribe
    public void onPlayerMovedEvent(PlayerMovedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        if(playerSession.getStatus()==null){ return;}
        if(playerSession.getStatus().getFuel() != null && playerSession.getStatus().getFuel().getFuelMain() > 0){
            // we are in the ship
            return;
        }
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation == null) {
            log.debug("Current location is null, skipping distance check.");
            return;
        }

        double[] landingCoordinates = currentLocation.getLandingCoordinates();
        if (landingCoordinates == null || landingCoordinates.length != 2) {
            log.debug("Landing coordinates invalid, skipping distance check.");
            return;
        }

        double latitude = event.getLatitude();
        double longitude = event.getLongitude();
        double planetRadius = event.getPlanetRadius();
        double lzLat = landingCoordinates[0];
        double lzLon = landingCoordinates[1];

        // Calculate great-circle distance (in meters)
        double distance = calculateSurfaceDistance(latitude, longitude, lzLat, lzLon, planetRadius, 0);

        // Define donut boundaries: 1.75km to 2km
        double innerDonut = 1750.0; // 1.75km in meters
        double outerDonut = 2000.0; // 2km in meters

        // Check if player is in the donut
        boolean isInDonut = distance >= innerDonut && distance <= outerDonut;

        if (isInDonut && !isInDonutArea) {
            EventBusManager.publish(
                    new VoiceProcessEvent(
                            String.format("Warning: You are %d meters from your ship, approaching auto-departure zone!",
                                    Math.round(distance))
                    )
            );
            log.info("Alert triggered: Player entered donut area at {} meters.", distance);
        }

        isInDonutArea = isInDonut;
    }

    @Subscribe
    public void onTouchdownEvent(TouchdownEvent event) {
        isInDonutArea = false;
        log.debug("State reset on touchdown.");
    }
}