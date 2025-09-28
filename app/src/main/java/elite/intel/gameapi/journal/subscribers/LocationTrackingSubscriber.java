package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.NavigationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.NavigationUtils.formatDistance;

public class LocationTrackingSubscriber {

    private static final Logger log = LogManager.getLogger(LocationTrackingSubscriber.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private double lastHeading = -1;
    private long lastAnnounceTime = 0;
    private Double cachedTepLat = null;
    private Double cachedTepLon = null;
    private boolean lastInTep = false;
    private double orbitalEntryAltitude = -1;
    private double detectedDropAltitude = -1;

    public static final int NORMAL_SPACE_HIGHEST_SPEED = 700; // Speeds above this are considered supercruise or orbital
    private static final long MIN_INTERVAL_MS = 20_000; // 20 sec base throttle
    private static final long ORBITAL_INTERVAL_MS = 40_000; // 40 sec for orbital cruise
    private static final long TEP_CLOSE_INTERVAL_MS = 5_000; // 5 sec when close to TEP
    private static final long GLIDE_APPROACH_INTERVAL_MS = 10_000; // 10 sec during glide approach for more frequent updates
    private static final double[] DISTANCE_THRESHOLDS = {1_000_000, 750_000, 500_000, 200_000, 100_000, 80_000, 50_000, 30_000, 20_000, 10_000, 5_000, 2_000, 1_500, 1_000, 500, 400, 300, 200, 150, 100, 75}; // meters, descending
    private static final double HIGH_SPEED_THRESHOLD = 1_000; // m/s
    private static final double HYSTERESIS = 10; // degrees
    private static final double APPROACH_RADIUS = 1_000; // meters
    private static final double ARRIVAL_RADIUS = 75; // meters
    private static final double TARGET_ENTRY_RADIUS = 300_000; // 300km for glide entry area (your 600km diameter)
    private static final double TEP_RADIUS = 1_500_000; // 1500km for TEP entry

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        TargetLocation targetLocation = playerSession.getTracking();
        if (!targetLocation.isEnabled()) {
            resetTrackingState();
            return;
        }

        if (lastTracking == null || !sameTarget(lastTracking, targetLocation)) {
            lastTracking = targetLocation;
            lastDistance = -1;
            lastHeading = -1;
            detectedDropAltitude = -1;
            cachedTepLat = null;
            cachedTepLon = null;
            lastInTep = false;
        }
        long now = System.currentTimeMillis();

        orbitalEntryAltitude = playerSession.getCurrentLocation().getOrbitalCruiseEntryAltitude();

        NavigationUtils.Direction navigator = NavigationUtils.getDirections(
                targetLocation.getLatitude(),
                targetLocation.getLongitude(),
                event
        );
        log.info("Directions: " + navigator.vocalization());
        EventBusManager.publish(new AppLogEvent(navigator.toString()));

        long announcementMinInterval = MIN_INTERVAL_MS;
        if (navigator.userSpeed() >= 15 && navigator.userSpeed() < 150) {
            announcementMinInterval = 10_000;
        } else if (navigator.userSpeed() > HIGH_SPEED_THRESHOLD) {
            announcementMinInterval = ORBITAL_INTERVAL_MS;
        }

        if (lastDistance == -1) {
            vocalize("Starting navigation to target. " + navigator.vocalization(), now);
            lastAnnounceTime = now;
            lastDistance = navigator.distanceToTarget();
            lastHeading = navigator.userHeading();
            return;
        }

        if (now - lastAnnounceTime < announcementMinInterval && navigator.distanceToTarget() > APPROACH_RADIUS) {
            lastDistance = navigator.distanceToTarget();
            lastHeading = navigator.userHeading();
            return;
        }

        if (isOnSurface(event, navigator)) {
            onSurfaceNavigation(navigator, now, announcementMinInterval);
        } else if (isInOrbit(event, navigator)) {
            inOrbitNavigation(now, orbitalEntryAltitude, announcementMinInterval, event, targetLocation);
        }

        lastDistance = navigator.distanceToTarget();
        lastHeading = navigator.userHeading();
    }

    private static boolean isInOrbit(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        return event.getAltitude() > 100 && navigator.userSpeed() > NORMAL_SPACE_HIGHEST_SPEED;
    }

    private static boolean isOnSurface(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        return event.getAltitude() == 0 || (navigator.userSpeed() > 0 && navigator.userSpeed() < NORMAL_SPACE_HIGHEST_SPEED);
    }

    private void inOrbitNavigation(long now, double orbitalEntryAltitude, long announcementMinInterval, PlayerMovedEvent event, TargetLocation targetLocation) {
        double targetLat = targetLocation.getLatitude();
        double targetLon = targetLocation.getLongitude();
        double planetRadius = event.getPlanetRadius();
        double altitude = event.getAltitude();

        NavigationUtils.Direction navigator = NavigationUtils.getDirections(targetLat, targetLon, event);
        int bearingToTarget = navigator.bearingToTarget();
        double distanceToTarget = navigator.distanceToTarget();
        int shipHeading = navigator.userHeading();

        if (cachedTepLon == null || cachedTepLat == null) {
            cachedTepLat = 0.0;
            cachedTepLon = NavigationUtils.calculateTepLongitude(targetLat, targetLon, planetRadius, altitude);
            log.info("TEP Set: lat=" + cachedTepLat + ", lon=" + cachedTepLon + ", targetLat=" + targetLat + ", targetLon=" + targetLon);
        }

        NavigationUtils.Direction tepNavigator = NavigationUtils.getDirections(cachedTepLat, cachedTepLon, event);
        double distanceToTep = tepNavigator.distanceToTarget();
        boolean inTep = distanceToTep <= TEP_RADIUS;

        long effectiveInterval = inTep ? GLIDE_APPROACH_INTERVAL_MS : (distanceToTep <= TEP_RADIUS ? TEP_CLOSE_INTERVAL_MS : announcementMinInterval);

        if (!lastInTep && inTep) {
            String tepAnnouncement = "Trajectory entry point reached. Adjust heading to " + bearingToTarget + " degrees, distance to glide point is " + formatDistance(distanceToTarget) + ". Pitch down -20 degrees to descend.";
            vocalize(tepAnnouncement, now);
            lastInTep = true;
        }

        log.info("TEP: lat=" + cachedTepLat + ", lon=" + cachedTepLon +
                ", distanceToTep=" + formatDistance(distanceToTep) +
                ", distanceToTarget=" + formatDistance(distanceToTarget) +
                ", altitude=" + formatDistance(altitude) +
                ", orbitalEntryAltitude=" + formatDistance(orbitalEntryAltitude) +
                ", inTep=" + inTep + ", announcementMinInterval=" + effectiveInterval +
                ", shipHeading=" + shipHeading + ", tepBearing=" + tepNavigator.bearingToTarget());

        if (!inTep) {
            if (Math.abs(tepNavigator.bearingToTarget() - shipHeading) > HYSTERESIS
                    && now - lastAnnounceTime >= effectiveInterval) {
                vocalize("Adjust heading to " + tepNavigator.bearingToTarget() + " degrees, distance to trajectory entry point is " +
                        formatDistance(distanceToTep) + ". Maintain altitude between orbital entry and drop point by alternating between +7 and -7 pitch to control speed.", now);
            }
        } else {
            if ((Math.abs(bearingToTarget - shipHeading) > HYSTERESIS || distanceToTarget < TARGET_ENTRY_RADIUS)
                    && now - lastAnnounceTime >= effectiveInterval) {
                String approachAnnouncement = "Adjust heading to " + bearingToTarget + " degrees, distance to glide point is " + formatDistance(distanceToTarget) + ".";
                if (distanceToTarget <= TARGET_ENTRY_RADIUS && altitude >= 100_000) {
                    double suggestedAngle = NavigationUtils.calculateGlideAngle(altitude, distanceToTarget);
                    approachAnnouncement += " Maintain glide angle around " + String.format("%.1f", suggestedAngle) + " degrees.";
                }
                vocalize(approachAnnouncement, now);
            }
        }

        if (lastDistance > TARGET_ENTRY_RADIUS && distanceToTarget <= TARGET_ENTRY_RADIUS) {
            vocalize("Approaching glide entry area. Adjust heading to " + bearingToTarget + " degrees, distance to glide point is " + formatDistance(distanceToTarget) + ". Pitch down to initiate glide.", now);
        }

        lastDistance = distanceToTarget;
    }

    private void onSurfaceNavigation(NavigationUtils.Direction navigator, long now, long effectiveInterval) {
        if (navigator.userHeading() != lastHeading && (navigator.bearingToTarget() > lastHeading + HYSTERESIS || navigator.bearingToTarget() < lastHeading - HYSTERESIS)
                && now - lastAnnounceTime >= effectiveInterval) {
            vocalize("Adjust heading: " + navigator.bearingToTarget() + " degrees.", now);
        }

        if (navigator.distanceToTarget() > lastDistance && now - lastAnnounceTime >= effectiveInterval) {
            vocalize(navigator.vocalization(), now);
        } else if (navigator.distanceToTarget() < lastDistance) {
            for (double th : DISTANCE_THRESHOLDS) {
                if (lastDistance > th && navigator.distanceToTarget() <= th) {
                    vocalize(formatDistance(th) + " from target.", now);
                    break;
                }
            }

            if (navigator.distanceToTarget() <= ARRIVAL_RADIUS && navigator.altitude() == 0) {
                vocalize("Arrived", now);
                TargetLocation t = playerSession.getTracking();
                t.setEnabled(false);
                playerSession.setTracking(t);
            }
        }
    }

    private void resetTrackingState() {
        lastTracking = null;
        lastDistance = -1;
        lastHeading = -1;
        lastAnnounceTime = 0;
        cachedTepLat = null;
        cachedTepLon = null;
        lastInTep = false;
    }

    private boolean sameTarget(TargetLocation a, TargetLocation b) {
        return a.equals(b);
    }

    private void vocalize(String textToVocalize, long now) {
        lastAnnounceTime = now;
        EventBusManager.publish(new TTSInterruptEvent());
        EventBusManager.publish(new VoiceProcessEvent(textToVocalize));
    }
}