package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.SupercruiseExitEvent;
import elite.intel.gameapi.journal.events.TouchdownEvent;
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
    private boolean onTheSurface = false;
    private double lastDistanceThreshold = 0;

    public static final int NORMAL_SPACE_HIGHEST_SPEED = 600;
    private static final long MIN_INTERVAL_MS = 10_000;
    private static final double[] DISTANCE_THRESHOLDS = generateDescendingSequence(10_000_000);
    private static final double HYSTERESIS = 7;
    private static final double ARRIVAL_RADIUS = 25;
    private static final double GLIDE_ENTRY_RADIUS = 400_000;
    private static final double TOO_FAR_FOR_GLIDE = 1_000_000;

    /**
     * Handles the event triggered when a player moves within the game environment.
     * This method processes the player's movement and navigational state
     * based on their proximity, heading, altitude, and speed relative to a target location.
     * It manages surface and orbital navigation, throttles announcements to avoid excessive updates,
     * and ensures proper state transitions for tracking logic.
     *
     * @param event An instance of {@code PlayerMovedEvent} containing the player's current
     *              latitude, longitude, altitude, and planetary radius data.
     */
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

        if (navigator.distanceToTarget() == 0 && navigator.altitude() == 0) {
            // we are not on the planet and not in orbit
            EventBusManager.publish(new AppLogEvent("Not on planet and not in orbit."));
            log.info("Navigation ON, but not on planet and not in orbit. Skipping navigation.");
            return;
        } else {
            EventBusManager.publish(new AppLogEvent(navigator.toString()));
            log.info(navigator.toString());
        }

        if (navigator.userSpeed() > 0 && event.getAltitude() < 10) {
            onTheSurface = true;
        } else if (navigator.userSpeed() > 0 && event.getAltitude() > 10) {
            onTheSurface = false;
        }


        long announcementMinInterval = MIN_INTERVAL_MS;
        if (navigator.userSpeed() >= 15 && navigator.userSpeed() < 150) {
            announcementMinInterval = 15_000;
        }

        // announcement time throttle
        if (NOW - lastAnnounceTime < announcementMinInterval) {
            lastDistance = navigator.distanceToTarget();
            lastHeading = navigator.userHeading();
            log.info("Skipping announcement. Reason: Timed throttle.");
            return;
        }

        if (isOnSurface(event, navigator)) {
            surfaceNavigation(navigator, NOW, announcementMinInterval, event.getAltitude());
        } else if (isInOrbit(event, navigator)) {
            orbitalNavigation(navigator, NOW, event);
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

    /**
     * Handles the orbital navigation logic to track the position and heading of a user
     * based on their proximity and trajectory relative to a target.
     *
     * @param navigator Provides utility functions for determining distance, bearings,
     *                  and heading of the user relative to a target.
     * @param now The current timestamp used for logging and timing-related calculations.
     * @param event The event containing updated player movement and altitude data.
     */
    private void orbitalNavigation(NavigationUtils.Direction navigator, long now, PlayerMovedEvent event) {
        // Orbital flight navigation.
        if (glideInitiated) return;

        int bearingToTarget = navigator.bearingToTarget();
        int shipHeading = navigator.userHeading();
        int glideAngle = calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget());

        double distanceToEdgeOfRadius = navigator.distanceToTarget() + GLIDE_ENTRY_RADIUS;
        double distanceToTarget = navigator.distanceToTarget();

        boolean movingAway = navigator.distanceToTarget() > lastDistance;
        boolean trajectoryDeviation = Math.abs(bearingToTarget - shipHeading) > HYSTERESIS;
        boolean glideAngleOk = distanceToTarget < TOO_FAR_FOR_GLIDE || glideAngle < 36;

        if (lastDistance == -1) {
            vocalize("Starting Orbital Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }

        if (trajectoryDeviation && distanceToTarget > GLIDE_ENTRY_RADIUS) {
            vocalize("", distanceToEdgeOfRadius, navigator.bearingToTarget(), now);
        } else if (!trajectoryDeviation && distanceToTarget > GLIDE_ENTRY_RADIUS) {
            if (glideAngleOk) {
                announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. Glide Angle:" + glideAngle + " degrees.", now);
            } else {
                announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. ", now);
            }
        } else if (distanceToTarget <= GLIDE_ENTRY_RADIUS && !glideInitiated) {
            if (glideAngleOk) {
                vocalize("Initiate Glide! Glide angle: " + glideAngle + ". ", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
            } else {
                vocalize("Too steep for safe glide. Reposition.", 0, 0, now);
            }

        } else {
            announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer", now);
        }

        lastDistance = distanceToTarget;
    }


    /**
     * Handles the surface navigation logic for low altitude flights or surface vehicles.
     * This method calculates the necessary bearings, glide angles, and distances
     * to assist in navigating towards a target while dealing with various thresholds,
     * such as altitude changes, speed, and proximity.
     *
     * @param navigator Provides utility functions for calculating distance, bearings,
     *                  speed, heading, and altitude of the user relative to the target.
     * @param now The current timestamp used for logging, announcements, and time-sensitive calculations.
     * @param effectiveInterval The minimum required interval for making repeated announcements, in milliseconds.
     * @param altitude Represents the current altitude of the user which determines specific response behaviors.
     */
    private void surfaceNavigation(NavigationUtils.Direction navigator, long now, long effectiveInterval, double altitude) {
        //Low altitude flights or Surface Recon Vehicle.
        if (lastDistance == -1) {
            if(navigator.userSpeed() > 0) {
                vocalize("Starting Surface Navigation", 0, 0, now);
            } else {
                vocalize("Starting Surface Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
            }
        }

        boolean headingDeviation = lastHeading > 0 && navigator.bearingToTarget() > lastHeading + HYSTERESIS || navigator.bearingToTarget() < lastHeading - HYSTERESIS;
        boolean aboveAnnouncementThreshold = now - lastAnnounceTime > effectiveInterval;
        boolean headingDoesMatchBearing = navigator.userHeading() != lastHeading;

        if (aboveAnnouncementThreshold && headingDoesMatchBearing && headingDeviation) {
            vocalize("", navigator.distanceToTarget(), navigator.bearingToTarget(), now);
        }

        boolean withinThreeKm = navigator.distanceToTarget() < 12_000;
        boolean tooFast = navigator.userSpeed() > 150;
        String reduceSpeed = "";
        if (withinThreeKm && tooFast) {
            reduceSpeed = " Reduce speed below 150 km. ";
        }

        int glideAngle = calculateGlideAngle(altitude, navigator.distanceToTarget());
        boolean movingAway = navigator.distanceToTarget() > lastDistance;

        if (altitude > 10) {
            if (navigator.distanceToTarget() < 1000) {
                vocalize("Within 1000 meters from target. Look for landing spot", 0, 0, now);
            } else {
                if (altitude > 3_000 && glideAngle < 45) {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. " + reduceSpeed + " Glide Angle:" + glideAngle + " degrees.", now);
                } else {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. " + reduceSpeed, now);
                }
            }
        } else if (onTheSurface) {
            announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. " + reduceSpeed, now);
        }


        if (onTheSurface && navigator.distanceToTarget() <= ARRIVAL_RADIUS && navigator.altitude() == 0 && navigator.userSpeed() < 700) {
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
        lastAnnounceTime = -1;
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
            if (current >= 10_000_000) {
                current -= 500_000; // Half of 1,000,000
            } else if (current >= 1_000_000) {
                current -= 250_000; // Quarter of 100,000
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

    @Subscribe
    public void onTouchdownEvent(TouchdownEvent event) {
        onTheSurface = true;
    }

}