package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.TTSInterruptEvent;
import elite.intel.ai.mouth.VocalisationSuccessfulEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.DisembarkEvent;
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

/**
 * The LocationTrackingSubscriber class is responsible for managing and processing
 * location tracking events based on a player's movement within the game environment.
 * It determines the player's positional and navigational states, handles transitions
 * between surface and orbital navigation, and manages announcements related to
 * tracking and movement.
 * <p>
 * This class includes methods to analyze player heading, altitude, and proximity to
 * various destinations, ensuring smooth user experience and timely notifications.
 * It supports glide path calculations, state transitions, and threshold-based
 * announcement systems.
 */
public class LocationTrackingSubscriber {

    private static final Logger log = LogManager.getLogger(LocationTrackingSubscriber.class);
    public static final int MAX_NORAMAL_SAPCE_SPEED = 700;
    public static final int APPROXIMATE_DRP_ALTITUDE = 30_000;
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private TargetLocation lastTracking = null;
    private double lastDistance = -1;
    private long lastAnnounceTime = -1;
    private double lastDistanceThreshold = 0;

    private static final long MIN_INTERVAL_MS = 12_000;
    private static final double[] DISTANCE_THRESHOLDS = generateDescendingSequence(10_000_000);
    private static final double HYSTERESIS = 7;
    private static final double ARRIVAL_RADIUS = 50;
    private static final double GLIDE_ENTRY_RADIUS = 400_000;
    private static final double TOO_FAR_FOR_GLIDE = 800_000;

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

        if (!targetLocation.equals(lastTracking) && lastTracking != null) {
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

        long announcementMinInterval = MIN_INTERVAL_MS;
        if (navigator.getSpeed() >= 15 && navigator.getSpeed() < 150) {
            announcementMinInterval = 17_000;
        }
        // announcement time throttle
        if (isAboveAnnouncementThreshold(NOW, announcementMinInterval)) {
            lastDistance = navigator.distanceToTarget();
            log.info("Skipping announcement. Reason: Timed throttle.");
            return;
        }

        if (isAboveSurface(event) || isOnSurface(event)) {
            surfaceNavigation(navigator, event);
        } else if (isInOrbit(event)) {
            orbitalNavigation(navigator, event);
        }

        lastDistance = navigator.distanceToTarget();
    }

    private boolean isOnSurface(PlayerMovedEvent event) {
        return event.getAltitude() < 10;
    }

    private boolean isInOrbit(PlayerMovedEvent event) {
        return event.getAltitude() > APPROXIMATE_DRP_ALTITUDE;
    }

    private boolean isAboveSurface(PlayerMovedEvent event) {
        return event.getAltitude() < APPROXIMATE_DRP_ALTITUDE;
    }


    /**
     * Handles the navigation of a player in orbital flight. This method ensures
     * the player's movement and trajectory adhere to specific navigational parameters
     * based on their altitude, distance to target, glide angle, and heading. It provides
     * relevant announcements and corrections to maintain a safe and efficient orbital path.
     *
     * @param navigator An instance of {@code NavigationUtils.Direction} representing
     *                  the navigation details such as bearing and distance to the target.
     * @param event     An instance of {@code PlayerMovedEvent} containing the player's
     *                  current position data including altitude and coordinates.
     */
    private void orbitalNavigation(NavigationUtils.Direction navigator, PlayerMovedEvent event) {
        // Orbital flight navigation.

        int glideAngle = - calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget());
        boolean glideAngleOk = isGlideAngleOk(event, navigator);
        boolean movingAway = navigator.distanceToTarget() > lastDistance;
        boolean trajectoryDeviation = isHeadingDeviation(navigator);

        double distanceToEdgeOfRadius = navigator.distanceToTarget() + GLIDE_ENTRY_RADIUS;
        double distanceToTarget = navigator.distanceToTarget();

        if (lastAnnounceTime < 1) {
            vocalize("Orbital Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), true);
        }

        if (trajectoryDeviation && distanceToTarget > GLIDE_ENTRY_RADIUS) {
            vocalize("", distanceToEdgeOfRadius, navigator.bearingToTarget(), false);
        } else if (!trajectoryDeviation && distanceToTarget > GLIDE_ENTRY_RADIUS && distanceToTarget < TOO_FAR_FOR_GLIDE) {
            if (glideAngleOk) {
                announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. Glide Angle:" + glideAngle + " degrees.");
            } else {
                announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. ");
            }
        } else if (distanceToTarget <= GLIDE_ENTRY_RADIUS) {
            if (glideAngleOk) {
                if (hasEnteredGlide(navigator, event)) {
                    vocalize("Glide angle:" + glideAngle + ". ", navigator.distanceToTarget(), navigator.bearingToTarget(), movingAway);
                } else {
                    if(event.getAltitude() > 100_000){
                        vocalize("Reduce altitude closer to DRP. ", navigator.distanceToTarget(), navigator.bearingToTarget(), true);
                    } else {
                        vocalize("Glide Zone! Glide angle: " + glideAngle + ". ", navigator.distanceToTarget(), navigator.bearingToTarget(), true);
                    }
                }
            } else {
                if(lastDistance > navigator.distanceToTarget()) { // do not announce if we are moving away.
                    vocalize("Too steep for safe glide. Reposition.", 0, 0, true);
                }
            }

        } else {
            announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer");
        }

        lastDistance = distanceToTarget;
    }

    private boolean isGlideAngleOk(PlayerMovedEvent event, NavigationUtils.Direction navigator) {

        if(event.getAltitude() > 100_000){
             return calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget()) < 60;
        } else if(event.getAltitude() < 50_000){
            return calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget()) < 45;
        } else {
            return calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget()) < 36;
        }
    }

    private boolean hasEnteredGlide(NavigationUtils.Direction navigator, PlayerMovedEvent event) {
        return navigator.distanceToTarget() > GLIDE_ENTRY_RADIUS
                && navigator.getSpeed() > MAX_NORAMAL_SAPCE_SPEED
                && event.getAltitude() > 20_000 && event.getAltitude() < 35_000;
    }


    /**
     * Handles the surface navigation of a player based on their movement and proximity to
     * a target location. It evaluates various factors including speed, heading deviation,
     * glide angle, and distance to provide navigational feedback and updates. Additionally,
     * it determines whether the player is approaching or moving away from the target, and
     * announces relevant guidance or warnings.
     *
     * @param navigator An instance of {@code NavigationUtils.Direction} representing navigation
     *                  parameters including distance and bearing to the target.
     * @param event     An instance of {@code PlayerMovedEvent} containing details about the player's
     *                  current position, altitude, and other movement data.
     */
    private void surfaceNavigation(NavigationUtils.Direction navigator, PlayerMovedEvent event) {
        //Low altitude flights or Surface Recon Vehicle.
        if (lastAnnounceTime < 1) {
            vocalize("Surface Navigation", navigator.distanceToTarget(), navigator.bearingToTarget(), true);
            return;
        }

        boolean headingDeviation = isHeadingDeviation(navigator);
        boolean glideAngleOk = isGlideAngleOk(event, navigator);
        speedWarning(navigator);

        int glideAngle = - calculateGlideAngle(event.getAltitude(), navigator.distanceToTarget());
        boolean movingAway = navigator.distanceToTarget() > lastDistance;

        if (isOnSurface(event)) {
            //CRAWLING on the surface (SRV or on foot)
            if (navigator.distanceToTarget() <= ARRIVAL_RADIUS && navigator.altitude() == 0 && navigator.getSpeed() < MAX_NORAMAL_SAPCE_SPEED) {
                vocalize("Arrived!", 0, 0, true);
                TargetLocation t = playerSession.getTracking();
                t.setEnabled(false);
                playerSession.setTracking(t);
                resetTrackingState();
            } else if (headingDeviation) {
                vocalize(movingAway ? "Moving Away." : "Getting Closer. ", navigator.distanceToTarget(), navigator.bearingToTarget(), movingAway);
            } else {
                announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer");
            }
        } else {
            //FLYING in normal space above surface
            if (navigator.distanceToTarget() < 1_000) {
                vocalize("Within 1000 meters from target. Look for landing spot", 0, 0, true);
            } else {
                if (headingDeviation) {
                    vocalize(movingAway ? "Moving Away." : "Getting Closer. ", navigator.distanceToTarget(), navigator.bearingToTarget(), movingAway);
                } else if (event.getAltitude() > 3_000 && glideAngleOk) {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. Glide Angle:" + glideAngle + " degrees.");
                } else {
                    announceDistances(navigator, movingAway ? "Moving Away." : "Getting Closer. ");
                }
            }
        }
    }

    private void speedWarning(NavigationUtils.Direction navigator) {
        if(navigator.distanceToTarget() <= 10_000 && navigator.getSpeed() >= 400){
            vocalize("Reduce speed below 350", 0, 0, true);
        } else if(navigator.distanceToTarget() <= 5_000 && navigator.getSpeed() >= 300){
            vocalize("Reduce speed below 200", 0, 0, true);
        }
    }

    private boolean isHeadingDeviation(NavigationUtils.Direction navigator) {
        return (Math.abs(navigator.bearingToTarget()) - navigator.userHeading()) > HYSTERESIS;
    }

    private boolean isAboveAnnouncementThreshold(long now, long effectiveInterval) {
        if (lastAnnounceTime < 1) return false;
        return now - lastAnnounceTime < effectiveInterval;
    }

    private void announceDistances(NavigationUtils.Direction navigator, String prefix) {
        for (double th : DISTANCE_THRESHOLDS) {
            if (lastDistance > th && navigator.distanceToTarget() <= th && lastDistanceThreshold != th) {
                lastDistanceThreshold = th;
                vocalize(prefix, navigator.distanceToTarget(), navigator.bearingToTarget(), false);
                break;
            }
        }
    }

    private void resetTrackingState() {
        playerSession.setTracking(new TargetLocation());
        lastTracking = null;
        lastDistance = -1;
        lastAnnounceTime = -1;
        lastDistanceThreshold = -1;
    }


    private void vocalize(String text, double distance, double bearing, boolean highPriority) {
        if (highPriority) EventBusManager.publish(new TTSInterruptEvent());
        StringBuilder sb = new StringBuilder();
        if (text != null) sb.append(text).append(". ");
        if (distance > 0) sb.append("Distance: ").append(formatDistance(distance)).append(". ");
        if (bearing > 0) sb.append("Bearing: ").append((int) bearing).append(" degrees").append(". ");
        log.info(sb.toString());
        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
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
    public void onDisembarkEvent(DisembarkEvent event) {
        resetTrackingState();
    }

    @Subscribe
    public void onVocalisationEvent(VocalisationSuccessfulEvent event) {
        lastAnnounceTime = System.currentTimeMillis();
    }
}