package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocationTrackingSubscriber {

    private static final Logger log = LogManager.getLogger(LocationTrackingSubscriber.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private double lastHeading = -1;
    private long lastAnnounceTime = 0;
    boolean announced = false;
    double enteringOrbitalCruiseAltitude = -1;

    private static final long MIN_INTERVAL_MS = 20000; // 20 sec base throttle, adjust as needed for TTS delay
    private static final double[] DISTANCE_THRESHOLDS = {1000000, 750000, 500000, 200000, 100000, 80000, 50000, 30000, 20000, 10000, 5000, 2000, 1500, 1000, 500, 400, 300, 200, 150, 100, 75}; // meters, descending
    private static final double HYSTERESIS = 10; // degrees
    private static final double APPROACH_RADIUS = 1000; // meters
    private static final double ARRIVAL_RADIUS = 75; // meters

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        TargetLocation tracking = playerSession.getTracking();
        if (!tracking.isEnabled()) {
            resetTrackingState();
            return;
        }

        // Check for new target
        if (lastTracking == null || !sameTarget(lastTracking, tracking)) {
            lastTracking = tracking;
            lastDistance = -1;
            lastHeading = -1;
        }
        long now = System.currentTimeMillis();


        //NOTE: playerSession.getCurrentLocation() is never null. (but could be blank dropFromOrbital will be 0)
        //NOTE: Orbital cruise altitude is known based on ApproachBodyEvent. We take altitude measure when that event fires and store it.
        enteringOrbitalCruiseAltitude = playerSession.getCurrentLocation().getOrbitalCruiseEntryAltitude();


        // Navigator (includes required bearing and distance to target, as well as user's current speed and current Heading)
        NavigationUtils.Direction navigator = NavigationUtils.getDirections(
                tracking.getLatitude(),
                tracking.getLongitude(),
                event
        );
        log.info("Directions: " + navigator.vocalization());


        long effectiveInterval = MIN_INTERVAL_MS;
        if (navigator.userSpeed() >= 15 && navigator.userSpeed() < 150) {
            effectiveInterval = 10000; // 10 sec
        }


        // Initial announcement for new tracking or first move
        if (lastDistance == -1) {
            EventBusManager.publish(new VoiceProcessEvent("Starting navigation to target. " + navigator.vocalization()));
            lastAnnounceTime = now;
            lastDistance = navigator.distanceToTarget();
            lastHeading = navigator.userHeading();
            return;
        }

        // Throttle unless close to target
        if (now - lastAnnounceTime < effectiveInterval && navigator.distanceToTarget() > APPROACH_RADIUS) {
            lastDistance = navigator.distanceToTarget(); // Still update for next checks
            lastHeading = navigator.userHeading();
            //log.info("throttle announcement");
            return;
        }




        ///  HEADING Announcement
        if (navigator.userHeading() != lastHeading && (navigator.bearingToTarget() > lastHeading + HYSTERESIS || navigator.bearingToTarget() < lastHeading - HYSTERESIS)
                && now - lastAnnounceTime >= effectiveInterval
        ) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust heading: " + navigator.bearingToTarget()+" degrees."));
            lastAnnounceTime = now;
            announced = true;
        }


        /// DISTANCE Announcement.
        // Check moving away
        if (navigator.distanceToTarget() > lastDistance && now - lastAnnounceTime >= effectiveInterval) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust: " + navigator.vocalization()));
            lastAnnounceTime = now;
            announced = true;
        } else if (navigator.distanceToTarget() < lastDistance) { // Approaching

            //TODO: change this. We need to skip DISTANCE_THRESHOLDS announcements if the speed is too fast to vocalize them all.

            // Check threshold crossings (highest first)
            for (double th : DISTANCE_THRESHOLDS) {
                if (lastDistance > th && navigator.distanceToTarget() <= th) {
                    //log.info("threshold crossed");
                    EventBusManager.publish(new VoiceProcessEvent(formatDistance(th) + " from target. "));
                    lastAnnounceTime = now;
                    announced = true;
                    break; // Only announce the highest crossed this time
                }
            }

            if (navigator.distanceToTarget() <= ARRIVAL_RADIUS) {
                EventBusManager.publish(new VoiceProcessEvent("Arrived."));
                lastAnnounceTime = now;
                TargetLocation t = playerSession.getTracking();
                t.setEnabled(false);
                playerSession.setTracking(t);
            }
        }

        lastDistance = navigator.distanceToTarget();
        lastHeading = navigator.userHeading();
    }

    private void resetTrackingState() {
        lastTracking = null;
        lastDistance = -1;
        lastHeading = -1;
        lastAnnounceTime = 0;
    }

    private boolean sameTarget(TargetLocation a, TargetLocation b) {
        return a.equals(b);
    }

    private String formatDistance(double d) {
        if (d >= 1000) {
            return String.format("%.1f kilometers", d / 1000);
        } else {
            return String.format("%.0f meters", d);
        }
    }
}