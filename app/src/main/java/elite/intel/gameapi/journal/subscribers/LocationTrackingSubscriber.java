package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.SupercruiseExitEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.NavigationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.NavigationUtils.calculateGlideAngle;
import static elite.intel.util.NavigationUtils.formatDistance;

public class LocationTrackingSubscriber {

    private static final Logger log = LogManager.getLogger(LocationTrackingSubscriber.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private double lastHeading = -1;
    private long lastAnnounceTime = 0;
    private boolean glideInitiated = false;
    private double lastDistanceThreshold = 0;

    public static final int NORMAL_SPACE_HIGHEST_SPEED = 600; // Speeds above this are considered supercruise or orbital
    private static final long MIN_INTERVAL_MS = 20_000; // 20 sec base throttle
    private static final double[] DISTANCE_THRESHOLDS = generateDescendingSequence(3_000_000);
    private static final double HYSTERESIS = 10; // degrees
    private static final double ARRIVAL_RADIUS = 75; // meters
    private static final double GLIDE_ENTRY_RADIUS = 300_000; // 300km for glide entry area

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {

        TargetLocation targetLocation = playerSession.getTracking();
        if (targetLocation == null || !targetLocation.isEnabled()) {
            resetTrackingState();
            return;
        }

        long NOW = System.currentTimeMillis();

        if (!targetLocation.equals(lastTracking)) {
            resetTrackingState();
            lastTracking = targetLocation;
        }



        NavigationUtils.Direction navigator = NavigationUtils.getDirections(
                targetLocation.getLatitude(),
                targetLocation.getLongitude(),
                event
        );

        EventBusManager.publish(new AppLogEvent(navigator.toString()));

        long announcementMinInterval = MIN_INTERVAL_MS;
        if (navigator.userSpeed() >= 15 && navigator.userSpeed() < 150) {
            announcementMinInterval = 30_000;
        }


        // announcement time throttle
        if (NOW - lastAnnounceTime < announcementMinInterval) {
            lastDistance = navigator.distanceToTarget();
            lastHeading = navigator.userHeading();
            log.info("Time throttled announcement.");
            return;
        }


        if (isOnSurface(event, navigator)) {
            onSurfaceNavigation(navigator, NOW, announcementMinInterval, event.getAltitude());
        } else if (isInOrbit(event, navigator)) {
            inOrbitNavigation(NOW, event, targetLocation);
        } else if (NOW - lastAnnounceTime < announcementMinInterval) {
            vocalize("Navigation will start once in orbit.", 0, 0, NOW);
        }

        lastDistance = navigator.distanceToTarget();
        lastHeading = navigator.userHeading();
    }

    private static boolean isInOrbit(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        return event.getAltitude() > 20_000 && navigator.userSpeed() > NORMAL_SPACE_HIGHEST_SPEED;
    }

    private static boolean isOnSurface(PlayerMovedEvent event, NavigationUtils.Direction navigator) {
        return event.getAltitude() == 0 || (navigator.userSpeed() > 0 && navigator.userSpeed() < NORMAL_SPACE_HIGHEST_SPEED);
    }

    private void inOrbitNavigation(long now, PlayerMovedEvent event, TargetLocation targetLocation) {
        if (glideInitiated) return;

        double targetLat = targetLocation.getLatitude();
        double targetLon = targetLocation.getLongitude();

        NavigationUtils.Direction navigator = NavigationUtils.getDirections(targetLat, targetLon, event);
        int bearingToTarget = navigator.bearingToTarget();
        double distanceToTarget = navigator.distanceToTarget();
        int shipHeading = navigator.userHeading();
        int glideAngle = calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget());

        if (lastDistance == -1) {
            vocalize("Starting Orbital Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }

        if (now - lastAnnounceTime > MIN_INTERVAL_MS) {
            announceDistances(navigator, event.getAltitude() > 50 ? "Destination: Glide point: Angle: " + glideAngle + " degrees." : "Target: ", now);
        }

        if ((Math.abs(bearingToTarget - shipHeading) > HYSTERESIS || distanceToTarget < GLIDE_ENTRY_RADIUS)) {
            vocalize("Destination: Glide Point", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }


        boolean movingAway = navigator.distanceToTarget() > lastDistance;
        if (distanceToTarget <= GLIDE_ENTRY_RADIUS && !glideInitiated) {
            glideInitiated = true;
            vocalize("Initiate Glide! Glide angle: " + glideAngle + ". ", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
            lastDistance = -1;
            return; // Surface nav will take over after glide.
        } else {
            announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. Glide Angle:" + glideAngle + " degrees.", now);
        }


        lastDistance = distanceToTarget;
    }

    private void onSurfaceNavigation(NavigationUtils.Direction navigator, long now, long effectiveInterval, double altitude) {

        if (lastDistance == -1) {
            vocalize("Starting Surface Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }

        boolean headingDeviation = lastHeading > 0 && navigator.bearingToTarget() > lastHeading + HYSTERESIS || navigator.bearingToTarget() < lastHeading - HYSTERESIS;
        boolean aboveAnnouncementThreshold = now - lastAnnounceTime > effectiveInterval;
        boolean headingDoesMatchBearing = navigator.userHeading() != lastHeading;

        if (aboveAnnouncementThreshold && headingDoesMatchBearing && headingDeviation) {
            vocalize("", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }

        int glideAngle = calculateGlideAngle(altitude, navigator.distanceToTarget());
        boolean movingAway = navigator.distanceToTarget() > lastDistance;

        if (altitude > 10) {
            if (navigator.distanceToTarget() < 1000) {
                vocalize("Within 1000 meters from target. Look for landing spot", 0, 0, now);
            } else {
                if(altitude > 1000) {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. Glide Angle:" + glideAngle + " degrees.", now);
                } else {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer", now);
                }
            }
        } else {
            announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. ", now);
        }


        if (navigator.distanceToTarget() <= ARRIVAL_RADIUS && navigator.altitude() == 0 && navigator.userSpeed() < 700) {
            vocalize("Arrived!", 0, 0, now);
            TargetLocation t = playerSession.getTracking();
            t.setEnabled(false);
            playerSession.setTracking(t);
            resetTrackingState();
        }
    }

    private void announceDistances(NavigationUtils.Direction navigator, String prefix, long now) {
        for (double th : DISTANCE_THRESHOLDS) {
            if (lastDistance > th && navigator.distanceToTarget() <= th && lastDistanceThreshold != th) {
                lastDistanceThreshold = th;
                vocalize(prefix, navigator.distanceToTarget(), navigator.bearingToTarget(), now);
                break;
            }
        }
    }

    private void resetTrackingState() {
        lastTracking = null;
        lastDistance = -1;
        lastHeading = -1;
        lastAnnounceTime = 0;
        glideInitiated = false;
        lastDistanceThreshold = -1;
    }


    private void vocalize(String text, double distance, double bearing, long now) {
        EventBusManager.publish(new TTSInterruptEvent());

        StringBuilder sb = new StringBuilder();
        if (text != null) sb.append(text).append(". ");
        if (distance > 0) sb.append("Distance: ").append(formatDistance(distance)).append(". ");
        if (bearing > 0) sb.append("Bearing: ").append((int) bearing).append(" degrees").append(". ");
        log.info(sb.toString());
        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
        lastAnnounceTime = now;
    }


    public static double[] generateDescendingSequence(double n) {
        List<Double> sequence = new ArrayList<>();
        double current = n;
        // Add initial value
        sequence.add(current);
        while (current > 0) {
            // Determine the current magnitude and corresponding step
            if (current >= 1_000_000) {
                current -= 250_000; // Quarter of 1,000,000
            } else if (current >= 100_000) {
                current -= 25_000; // Quarter of 100,000
            } else if (current >= 10_000) {
                current -= 2_500; // Quarter of 10,000
            } else if (current >= 1_000) {
                current -= 250; // Quarter of 1,000
            } else if (current >= 100) {
                current -= 25; // Quarter of 100
            } else {
                current -= 25; // Continue with 25 until 0
            }
            // Ensure we don't go negative
            if (current < 0) {
                current = 0;
            }
            sequence.add(current);
        }

        // Convert List<Double> to double[]
        double[] result = new double[sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            result[i] = sequence.get(i);
        }

        return result;
    }

    @Subscribe
    public void onSuperCruiseExit(SupercruiseExitEvent event) {
        glideInitiated = true;
    }

}