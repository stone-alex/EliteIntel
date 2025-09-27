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
    private PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private double lastHeading = -1;
    private long lastAnnounceTime = 0;
//    private double lastLatitude = 0;
//    private double lastLongitude = 0;
//    private long lastMoveTime = 0;

    private static final long MIN_INTERVAL_MS = 20000; // 20 sec base throttle, adjust as needed for TTS delay
    private static final double[] DISTANCE_THRESHOLDS = {500000, 200000, 100000, 80000, 50000, 30000, 20000, 10000, 5000, 2000, 1500, 1000, 500, 400, 300, 200, 150, 100, 75}; // meters, descending
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

        // Calculate directions (includes heading distance and current speed)
        NavigationUtils.Direction directions = NavigationUtils.getDirections(
                tracking.getLatitude(),
                tracking.getLongitude(),
                event.getLatitude(),
                event.getLongitude(),
                event.getPlanetRadius()
        );
        log.info("Directions: " + directions.toString());


        long now = System.currentTimeMillis();
        //TODO: change this. We need to skip DISTANCE_THRESHOLDS announcements if the speed is too fast to vocalize them all.
        long effectiveInterval = MIN_INTERVAL_MS;
        if (directions.userSpeed() >= 15 && directions.userSpeed() < 150) {
            effectiveInterval = 10000; // 10 sec
        }

        log.info("Effective Interval: " + effectiveInterval);

        // Initial announcement for new tracking or first move
        if (lastDistance == -1) {
            EventBusManager.publish(new VoiceProcessEvent("Starting navigation to target. " + directions.vocalization()));
            lastAnnounceTime = now;
            lastDistance = directions.distanceToTarget();
            lastHeading = directions.userHeading();
            return;
        }



        // Throttle unless close to target
        if (now - lastAnnounceTime < effectiveInterval && directions.distanceToTarget() > APPROACH_RADIUS) {
            lastDistance = directions.distanceToTarget(); // Still update for next checks
            lastHeading = directions.userHeading();
            log.info("throttle announcement");
            return;
        }

        boolean announced = false;


        ///  HEADING
        if (directions.userHeading() != lastHeading && (directions.headingToTarget() > lastHeading + HYSTERESIS || directions.headingToTarget() < lastHeading - HYSTERESIS)
                && now - lastAnnounceTime >= effectiveInterval
        ) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust heading: " + directions.headingToTarget()+" degrees."));
            lastAnnounceTime = now;
            announced = true;
        }

        /// DISTANCE
        // Check moving away
        if (directions.distanceToTarget() > lastDistance && now - lastAnnounceTime >= effectiveInterval) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust: " + directions.vocalization()));
            lastAnnounceTime = now;
            announced = true;
        } else if (directions.distanceToTarget() < lastDistance) { // Approaching
            // Check threshold crossings (highest first)
            for (double th : DISTANCE_THRESHOLDS) {
                log.info("threshold crossing check lastDistance: " + lastDistance + " threshold: " + th + " distance: " + directions.distanceToTarget());
                if (lastDistance > th && directions.distanceToTarget() <= th) {
                    log.info("threshold crossed");
                    EventBusManager.publish(new VoiceProcessEvent(formatDistance(th) + " from target. "));
                    lastAnnounceTime = now;
                    announced = true;
                    break; // Only announce the highest crossed this time
                }
            }

            // Approach and arrival (override throttle if needed)
            if (!announced && lastDistance > APPROACH_RADIUS && directions.distanceToTarget() <= APPROACH_RADIUS) {
                log.info("approach check");
                EventBusManager.publish(new VoiceProcessEvent("Approaching target, " + formatDistance(directions.distanceToTarget()) + " remaining."));
                lastAnnounceTime = now;
                announced = true;
            }

            if (directions.distanceToTarget() <= ARRIVAL_RADIUS) {
                log.info("arrival check");
                EventBusManager.publish(new VoiceProcessEvent("Arrived."));
                lastAnnounceTime = now;
                TargetLocation t = playerSession.getTracking();
                t.setEnabled(false);
                playerSession.setTracking(t);
            }
        }

        lastDistance = directions.distanceToTarget();
        lastHeading = directions.userHeading();
    }

    private void resetTrackingState() {
        lastTracking = null;
        lastDistance = -1;
        lastHeading = -1;
        lastAnnounceTime = 0;
    }

    private boolean sameTarget(TargetLocation a, TargetLocation b) {
        // Assuming TargetLocation doesn't have equals(), compare coords (add equals() if possible)
        return a.getLatitude() == b.getLatitude() && a.getLongitude() == b.getLongitude() && a.getRequestedTime() == b.getRequestedTime();
    }

    private String formatDistance(double d) {
        if (d >= 1000) {
            return String.format("%.1f kilometers", d / 1000);
        } else {
            return String.format("%.0f meters", d);
        }
    }
}