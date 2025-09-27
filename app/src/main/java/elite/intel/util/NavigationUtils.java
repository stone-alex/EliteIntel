package elite.intel.util;

import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;

import static elite.intel.util.DistanceCalculator.calculateSurfaceDistance;

public class NavigationUtils {

    private static long lastMoveTime = 0;
    private static double lastLatitude = -1;
    private static double lastLongitude = -1;


    public static Direction getDirections(double targetLatitude, double targetLongitude, PlayerMovedEvent event) {

        Result resultForDirections = calculateHeading(targetLatitude, targetLongitude, event.getLatitude(), event.getLongitude(), event.getPlanetRadius());
        Result resultForCurrentPosition = calculateHeading(event.getLatitude(), event.getLongitude(), lastLatitude, lastLongitude, event.getPlanetRadius());
        double speed = calculateSpeed(event.getLatitude(), event.getLongitude(), event.getPlanetRadius());

        return new Direction(
                resultForDirections.roundedBearing(), // heading to target
                Math.round(resultForCurrentPosition.roundedBearing()), // your heading
                resultForDirections.distanceInMeters(), // distance to target
                Math.round(speed),
                "Heading " + resultForDirections.roundedBearing() + " degrees, distance to target is " + formatDistance(resultForDirections.distanceInMeters())
        );
    }

    private static double calculateSpeed(double userLatitude, double userLongitude, double planetRadius) {
        long now = System.currentTimeMillis();
        double speed = 0;
        if (lastMoveTime > 0) {
            double deltaTimeSec = (now - lastMoveTime) / 1000.0; // seconds
            double deltaDist = calculateSurfaceDistance(
                    lastLatitude,
                    lastLongitude,
                    userLatitude,
                    userLongitude,
                    planetRadius
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

    private static Result calculateHeading(double targetLatitude, double targetLongitude, double userLatitude, double userLongitude, double planetRadius) {
        // Convert degrees to radians for trigonometric calculations
        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(targetLatitude);
        double lon2 = Math.toRadians(targetLongitude);

        // Calculate the difference in longitude
        double deltaLon = lon2 - lon1;

        // Calculate the initial bearing using spherical trigonometry
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);

        // Convert bearing from radians to degrees and normalize to 0-360
        double bearingDegrees = Math.toDegrees(bearing);
        bearingDegrees = (bearingDegrees + 360) % 360;
        int roundedBearing = (int) Math.round(bearingDegrees);

        return new Result(roundedBearing, calculateSurfaceDistance(targetLatitude, targetLongitude, userLatitude, userLongitude, planetRadius));
    }

    private record Result(int roundedBearing, double distanceInMeters) {
    }


    public record Direction(int headingToTarget, int userHeading, double distanceToTarget, double userSpeed, String vocalization) {
        public String toString() {
            return "Heading to target: " + headingToTarget + " Your Heading " + userHeading + " Distance: " + distanceToTarget + " Speed: " + userSpeed;
        }
    }

    private static String formatDistance(double d) {
        if (d >= 1000) {
            return String.format("%.1f kilometers", d / 1000);
        } else {
            return String.format("%.0f meters", d);
        }
    }
}