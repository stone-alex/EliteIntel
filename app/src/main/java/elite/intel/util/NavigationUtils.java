package elite.intel.util;

import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavigationUtils {

    private static final Logger log = LogManager.getLogger(NavigationUtils.class);

    private static long lastMoveTime = 0;
    private static double lastLatitude = -1;
    private static double lastLongitude = -1;
    private static int lastValidHeading = 0; // Persist last valid heading

    public static Direction getDirections(double targetLatitude, double targetLongitude, PlayerMovedEvent event) {

        Result resultForDirections = calculateHeading(targetLatitude, targetLongitude, event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude());
        int bearingToTarget = calculateRhumbBearing(event.getLatitude(), event.getLongitude(), targetLatitude, targetLongitude);
        Result resultForCurrentPosition = calculateHeading(event.getLatitude(), event.getLongitude(), lastLatitude, lastLongitude, event.getPlanetRadius(), event.getAltitude());
        double speed = calculateSpeed(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude());
        int userHeading = resultForCurrentPosition.roundedBearing() != 0 ? resultForCurrentPosition.roundedBearing() : lastValidHeading;

        // Update last valid heading
        if (resultForCurrentPosition.roundedBearing() != 0) {
            lastValidHeading = resultForCurrentPosition.roundedBearing();
        }

        log.info("Nav Debug: Rhumb bearing=" + bearingToTarget +
                ", Great-circle bearing=" + resultForDirections.roundedBearing() +
                ", Distance=" + formatDistance(resultForDirections.distanceInMeters()) +
                ", Altitude=" + formatDistance(event.getAltitude()) +
                ", UserHeading=" + userHeading +
                ", LastLat=" + lastLatitude + ", LastLon=" + lastLongitude +
                ", CurrLat=" + event.getLatitude() + ", CurrLon=" + event.getLongitude());

        return new Direction(
                bearingToTarget,
                resultForDirections.distanceInMeters(),
                userHeading,
                Math.round(speed),
                event.getAltitude(),
                "Heading " + bearingToTarget + " degrees, distance to target is " + formatDistance(resultForDirections.distanceInMeters())
        );
    }

    private static int calculateRhumbBearing(double userLatitude, double userLongitude, double targetLatitude, double targetLongitude) {
        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(targetLatitude);
        double lon2 = Math.toRadians(targetLongitude);
        double deltaLon = lon2 - lon1;
        double deltaPhi;
        try {
            deltaPhi = Math.log(Math.tan(Math.PI / 4 + lat2 / 2) / Math.tan(Math.PI / 4 + lat1 / 2));
        } catch (ArithmeticException e) {
            return calculateHeading(userLatitude, userLongitude, targetLatitude, targetLongitude, 0, 0).roundedBearing();
        }
        double bearing = Math.atan2(deltaLon, deltaPhi);
        double bearingDegrees = Math.toDegrees(bearing);
        bearingDegrees = (bearingDegrees + 360) % 360;
        return (int) Math.round(bearingDegrees);
    }

    private static double calculateSpeed(double userLatitude, double userLongitude, double planetRadius, double altitude) {
        long now = System.currentTimeMillis();
        double speed = 0;
        if (lastMoveTime > 0 && lastLatitude != -1 && lastLongitude != -1) {
            double deltaTimeSec = (now - lastMoveTime) / 1000.0;
            double deltaDist = calculateSurfaceDistance(
                    lastLatitude,
                    lastLongitude,
                    userLatitude,
                    userLongitude,
                    planetRadius,
                    altitude
            );
            if (deltaTimeSec > 0) {
                speed = deltaDist / deltaTimeSec;
            }
        }
        lastMoveTime = now;
        lastLatitude = userLatitude;
        lastLongitude = userLongitude;
        return speed;
    }

    private static Result calculateHeading(double targetLatitude, double targetLongitude, double userLatitude, double userLongitude, double planetRadius, double altitude) {
        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(targetLatitude);
        double lon2 = Math.toRadians(targetLongitude);
        double deltaLon = lon2 - lon1;
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);
        double bearingDegrees = Math.toDegrees(bearing);
        bearingDegrees = (bearingDegrees + 360) % 360;
        int roundedBearing = (int) Math.round(bearingDegrees);
        return new Result(roundedBearing, calculateSurfaceDistance(targetLatitude, targetLongitude, userLatitude, userLongitude, planetRadius, altitude));
    }

    public static double calculateGalacticDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static double calculateSurfaceDistance(double lat1, double lon1, double lat2, double lon2, double radius, double altitude) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (radius + altitude) * c;
    }

    public static int calculateGlideAngle(double altitude, double distanceToTarget) {
        // Check for invalid inputs (non-positive altitude or distance)
        if (altitude <= 0 || distanceToTarget <= 0) {
            return 0;
        }

        // Calculate the angle using arctangent (atan) and convert radians to degrees
        double angleInRadians = Math.atan(altitude / distanceToTarget);
        double angleInDegrees = Math.toDegrees(angleInRadians);

        // Round to nearest integer and return
        return (int) Math.round(angleInDegrees);
    }



    public static String formatDistance(double d) {
        if (d >= 1000) {
            return String.format("%.1f kilometers", d / 1000);
        } else {
            return String.format("%.0f meters", d);
        }
    }

    private record Result(int roundedBearing, double distanceInMeters) {
    }

    public record Direction(int bearingToTarget, double distanceToTarget, int userHeading, double userSpeed, double altitude, String vocalization) {
        public String toString() {
            return "Heading to target: " + bearingToTarget +
                    " Your Heading " + userHeading +
                    " Distance: " + formatDistance(distanceToTarget) +
                    " Speed: " + userSpeed +
                    " Altitude: " + formatDistance(altitude);
        }
    }
}