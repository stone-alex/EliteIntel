package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.NavigationVocalisationEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.status_events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.DockSRVEvent;
import elite.intel.gameapi.journal.events.LaunchSRVEvent;
import elite.intel.gameapi.journal.events.TouchdownEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;


public class DistanceFromShipTracker {

    private static final Logger log = LogManager.getLogger(DistanceFromShipTracker.class);
    private boolean shouldAnnounce = true;
    private long lastAnnounceTime = 0;

    @Subscribe
    public void onPlayerMovedEvent(PlayerMovedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        if(status.getStatus()==null){ return;}
        if(status.getStatus().getFuel() != null && status.getStatus().getFuel().getFuelMain() > 0){
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
        Double distance = calculateSurfaceDistance(latitude, longitude, lzLat, lzLon, planetRadius, 0);

        // Define donut boundaries: 1.8km to 2km
        double innerDonut = 1800.0; // 1.8km in meters
        double outerDonut = 2000.0; // 2km in meters

        // Check if player is in the donut
        boolean isInDonut = distance >= innerDonut && distance <= outerDonut;
        long NOW = System.currentTimeMillis();
        if (isInDonut && shouldAnnounce && status.getStatus().getAltitude() == 0 && NOW - lastAnnounceTime < 15_000) {
            EventBusManager.publish(
                    new NavigationVocalisationEvent(
                            String.format("Warning: You are %d meters from your ship, approaching auto-departure zone!",
                                    Math.round(distance))
                    )
            );
            log.info("Alert triggered: Player entered donut area at {} meters.", distance);
        }
        // when outside, and ship departed, no reason to announce again
        shouldAnnounce = distance > outerDonut;
    }

    @Subscribe
    public void onTouchdownEvent(TouchdownEvent event) {
        shouldAnnounce = false;
        lastAnnounceTime = 0;
        log.debug("State reset on touchdown.");
    }

    @Subscribe
    public void onDockSRV(DockSRVEvent event) {
        shouldAnnounce = false;
        lastAnnounceTime = 0;
        log.debug("State reset on dock.");
    }

    @Subscribe
    public void onSrvLaunch(LaunchSRVEvent event) {
        shouldAnnounce = true;
        lastAnnounceTime = 0;
        log.debug("State reset on srv launch.");
    }
}