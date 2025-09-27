package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.openai.OpenAiChatEndPoint;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.util.DistanceCalculator;
import elite.intel.util.NavigationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocationTrackingSubscriber {

    private static final Logger log = LogManager.getLogger(LocationTrackingSubscriber.class);
    private PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private long lastAnnounceTime = 0;
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    private long lastMoveTime = 0;

    private static final long MIN_INTERVAL_MS = 15000; // 15 sec base throttle, adjust as needed for TTS delay
    private static final double[] DISTANCE_THRESHOLDS = {50000, 30000, 20000, 10000, 5000, 2000, 1000, 500, 400, 300, 200, 150, 100, 75, 50, 25}; // meters, descending
    private static final double HYSTERESIS = 25; // meters, to avoid jitter
    private static final double APPROACH_RADIUS = 1000; // meters
    private static final double ARRIVAL_RADIUS = 250; // meters

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        TargetLocation tracking = playerSession.getTracking();
        if (tracking == null || tracking.getLatitude() == 0 && tracking.getLongitude() == 0) {
            resetTrackingState();
            log.info("Nothing to track");
            return;
        }

        // Check for new target
        if (lastTracking == null || !sameTarget(lastTracking, tracking)) {
            lastTracking = tracking;
            lastDistance = -1;
        }

        // Calculate distance to target
        double distance = DistanceCalculator.calculateSurfaceDistance(
                tracking.getLatitude(),
                tracking.getLongitude(),
                event.getLatitude(),
                event.getLongitude(),
                event.getPlanetRadius()
        );
        log.info("Distance to target: " + distance);

        // Calculate directions (includes heading and distance)
        String directions = NavigationUtils.getHeading(
                tracking.getLatitude(),
                tracking.getLongitude(),
                event.getLatitude(),
                event.getLongitude(),
                event.getPlanetRadius()
        );
        log.info("Directions: " + directions);


        // Optional: Calculate speed (m/s) for potential dynamic adjustments
        long now = System.currentTimeMillis();
        double speed = 0;
        if (lastMoveTime > 0) {
            double deltaTimeSec = (now - lastMoveTime) / 1000.0;
            double deltaDist = DistanceCalculator.calculateSurfaceDistance(
                    lastLatitude,
                    lastLongitude,
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getPlanetRadius()
            );
            if (deltaTimeSec > 0) {
                speed = deltaDist / deltaTimeSec;
            }
        }
        lastLatitude = event.getLatitude();
        lastLongitude = event.getLongitude();
        lastMoveTime = now;
        log.info("Speed: " + speed);

        // Optional: Dynamic throttle based on speed (e.g., slower movement = shorter interval for more frequent guidance)
        long effectiveInterval = MIN_INTERVAL_MS;
        if (speed > 0 && speed < 5) { // Very slow SRV crawling
            effectiveInterval = 1000; // 1 sec
        } else if (speed >= 5 && speed < 15) { // Typical SRV cruising
            effectiveInterval = 3000; // 3 sec
        } else if (speed >= 15 && speed < 150) { // Slow flying
            effectiveInterval = 7000; // 7 sec
        } else if(speed >= 150) {
            effectiveInterval = 10000; // 10 sec
        }

        log.info("Effective Interval: " + effectiveInterval);

        // Initial announcement for new tracking or first move
        if (lastDistance == -1) {
            EventBusManager.publish(new VoiceProcessEvent("Starting navigation to target. " + directions));
            lastAnnounceTime = now;
            lastDistance = distance;
            return;
        }

        // Throttle unless close to target
        if (now - lastAnnounceTime < effectiveInterval && distance > APPROACH_RADIUS) {
            lastDistance = distance; // Still update for next checks
            log.info("throttle announcement");
            return;
        }

        boolean announced = false;

        // Check moving away
        if (distance > lastDistance + HYSTERESIS && now - lastAnnounceTime >= effectiveInterval) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust: " + directions));
            lastAnnounceTime = now;
            announced = true;
            log.info("Adjust Announcement");
        } else if (distance < lastDistance - HYSTERESIS) { // Approaching
            // Check threshold crossings (highest first)
            for (double th : DISTANCE_THRESHOLDS) {
                log.info("threshold crossing check lastDistance: "+lastDistance+" threshold: "+th+" distance: "+distance);
                if (lastDistance > th && distance <= th) {
                    log.info("threshold crossed");
                    EventBusManager.publish(new VoiceProcessEvent(formatDistance(th) + " from target. "));
                    lastAnnounceTime = now;
                    announced = true;
                    break; // Only announce the highest crossed this time
                }
            }

            // Approach and arrival (override throttle if needed)
            if (!announced && lastDistance > APPROACH_RADIUS && distance <= APPROACH_RADIUS) {
                log.info("approach check");
                EventBusManager.publish(new VoiceProcessEvent("Approaching target, " + formatDistance(distance) + " remaining."));
                lastAnnounceTime = now;
                announced = true;
            }

            if (!announced && lastDistance > ARRIVAL_RADIUS && distance <= ARRIVAL_RADIUS) {
                log.info("arrival check");
                EventBusManager.publish(new VoiceProcessEvent("Arrived."));
                lastAnnounceTime = now;
                playerSession.setTracking(new TargetLocation());
            }
        }

        lastDistance = distance;
    }

    private void resetTrackingState() {
        lastTracking = null;
        lastDistance = -1;
        lastAnnounceTime = 0;
        lastMoveTime = 0;
    }

    private boolean sameTarget(TargetLocation a, TargetLocation b) {
        // Assuming TargetLocation doesn't have equals(), compare coords (add equals() if possible)
        return a.getLatitude() == b.getLatitude() && a.getLongitude() == b.getLongitude();
    }

    private String formatDistance(double d) {
        if (d >= 1000) {
            return String.format("%.1f kilometers", d / 1000);
        } else {
            return String.format("%.0f meters", d);
        }
    }
}