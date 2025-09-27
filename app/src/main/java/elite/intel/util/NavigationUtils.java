package elite.intel.util;

public class NavigationUtils {

    private static long lastMoveTime = 0;
    private static double lastLatitude = -1;
    private static double lastLongitude = -1;


    /**
     * Calculates the compass heading and distance from the player's position to a target location
     * and returns a vocalization-friendly string (e.g., "Set heading to 354 degrees, distance to target is 624 meters").
     *
     * @param entryLatitude  Latitude of the target location in degrees
     * @param entryLongitude Longitude of the target location in degrees
     * @param userLatitude   Latitude of the player's current position in degrees
     * @param userLongitude  Longitude of the player's current position in degrees
     * @param planetRadius   Radius of the planet in meters
     * @return A string representing the heading and distance for vocalization
     */
    public static Direction getDirections(double entryLatitude, double entryLongitude, double userLatitude, double userLongitude, double planetRadius) {

        Result resultForDirections = calculateHeading(entryLatitude, entryLongitude, userLatitude, userLongitude, planetRadius);

        Result resultForCurrentPosition = calculateHeading(userLatitude, userLongitude, lastLatitude, lastLongitude, planetRadius);

        double speed = calculateSpeed(userLatitude, userLongitude, planetRadius);

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
            double deltaDist = DistanceCalculator.calculateSurfaceDistance(
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

    private static Result calculateHeading(double entryLatitude, double entryLongitude, double userLatitude, double userLongitude, double planetRadius) {
        // Convert degrees to radians for trigonometric calculations
        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(entryLatitude);
        double lon2 = Math.toRadians(entryLongitude);

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

        // Calculate great-circle distance using the haversine formula
        double planetRadiusInMeters = planetRadius;
        double deltaLat = lat2 - lat1;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMeters = planetRadiusInMeters * c; // Distance in kilometers

        return new Result(roundedBearing, distanceMeters);
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