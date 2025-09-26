package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.util.DistanceCalculator;
import elite.intel.util.NavigationUtils;

public class LocationTrackingSubscriber {

    private PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private long lastAnnounceTime = 0;
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    private long lastMoveTime = 0;

    private static final long MIN_INTERVAL_MS = 15000; // 15 sec base throttle, adjust as needed for TTS delay
    private static final double[] DISTANCE_THRESHOLDS = {10000, 5000, 2000, 1000, 500, 200, 100, 50}; // meters, descending
    private static final double HYSTERESIS = 50; // meters, to avoid jitter
    private static final double APPROACH_RADIUS = 50; // meters
    private static final double ARRIVAL_RADIUS = 10; // meters

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        TargetLocation tracking = playerSession.getTracking();
        if (tracking == null) {
            resetTrackingState();
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

        // Calculate directions (includes heading and distance)
        String directions = NavigationUtils.getHeading(
                tracking.getLatitude(),
                tracking.getLongitude(),
                event.getLatitude(),
                event.getLongitude(),
                event.getPlanetRadius()
        );

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

        // Optional: Dynamic throttle based on speed (e.g., slower movement = shorter interval for more frequent guidance)
        long effectiveInterval = MIN_INTERVAL_MS;
        if (speed > 0 && speed < 10) { // e.g., crawling slow <10 m/s, allow more often
            effectiveInterval = 10000; // 10 sec
        }

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
            return;
        }

        boolean announced = false;

        // Check moving away
        if (distance > lastDistance + HYSTERESIS) {
            EventBusManager.publish(new VoiceProcessEvent("Adjust: " + directions));
            lastAnnounceTime = now;
            announced = true;
        } else if (distance < lastDistance - HYSTERESIS) { // Approaching
            // Check threshold crossings (highest first)
            for (double th : DISTANCE_THRESHOLDS) {
                if (lastDistance > th && distance <= th) {
                    EventBusManager.publish(new VoiceProcessEvent("Now " + formatDistance(th) + " from target. " + directions));
                    lastAnnounceTime = now;
                    announced = true;
                    break; // Only announce the highest crossed this time
                }
            }

            // Approach and arrival (override throttle if needed)
            if (!announced && lastDistance > APPROACH_RADIUS && distance <= APPROACH_RADIUS) {
                EventBusManager.publish(new VoiceProcessEvent("Approaching target, " + formatDistance(distance) + " remaining."));
                lastAnnounceTime = now;
                announced = true;
            }

            if (!announced && lastDistance > ARRIVAL_RADIUS && distance <= ARRIVAL_RADIUS) {
                EventBusManager.publish(new VoiceProcessEvent("Arrived."));
                lastAnnounceTime = now;
                playerSession.setTracking(null);
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