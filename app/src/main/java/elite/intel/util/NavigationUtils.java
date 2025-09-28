package elite.intel.util;

import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;

/**
 * Utility class for navigation-related calculations, such as determining direction,
 * distance, and speed based on geographical positions.
 */
public class NavigationUtils {

    private static long lastMoveTime = 0;
    private static double lastLatitude = -1;
    private static double lastLongitude = -1;

    /**
     * Computes the direction and related movement information for a player towards a target location.
     * This includes heading to the target, distance to the target, player's current heading, speed, altitude,
     * and a verbalized description of the movement details.
     *
     * @param targetLatitude the latitude of the target location in degrees
     * @param targetLongitude the longitude of the target location in degrees
     * @param event an instance of PlayerMovedEvent containing the current player's position, planet radius, and altitude
     * @return a Direction object that contains the heading to the target, distance to the target, current heading,
     *         current speed, altitude, and a verbalized description of the movement
     */
    public static Direction getDirections(double targetLatitude, double targetLongitude, PlayerMovedEvent event) {

        Result resultForDirections = calculateHeading(targetLatitude, targetLongitude, event.getLatitude(), event.getLongitude(), event.getPlanetRadius());
        Result resultForCurrentPosition = calculateHeading(event.getLatitude(), event.getLongitude(), lastLatitude, lastLongitude, event.getPlanetRadius());
        double speed = calculateSpeed(event.getLatitude(), event.getLongitude(), event.getPlanetRadius());

        return new Direction(
                resultForDirections.roundedBearing(), // heading to target
                resultForDirections.distanceInMeters(), // distance to target
                Math.round(resultForCurrentPosition.roundedBearing()), // your heading
                Math.round(speed),
                event.getAltitude(),
                "Heading " + resultForDirections.roundedBearing() + " degrees, distance to target is " + formatDistance(resultForDirections.distanceInMeters())
        );
    }

    /**
     * Calculates the speed of movement based on the distance traveled and time elapsed
     * since the last recorded position. The method uses the haversine formula to compute
     * the great-circle distance between two geographical points on a planet's surface.
     *
     * @param userLatitude  the current latitude of the user in degrees
     * @param userLongitude the current longitude of the user in degrees
     * @param planetRadius  the radius of the planet in meters
     * @return the speed of movement in meters per second
     */
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

    /**
     * Calculates the heading (bearing) and surface distance from a user's current location
     * to a target location on a planetary surface. The heading is returned in degrees,
     * normalized to the range [0, 360). The distance is calculated using the haversine formula.
     *
     * @param targetLatitude   the latitude of the target location in degrees
     * @param targetLongitude  the longitude of the target location in degrees
     * @param userLatitude     the latitude of the user's current location in degrees
     * @param userLongitude    the longitude of the user's current location in degrees
     * @param planetRadius     the radius of the planet in meters
     * @return a Result object containing the rounded heading in degrees and the distance in meters
     */
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


    /**
     * Calculates the distance in light years between two systems using galaxy map coordinates.
     *
     * @param x1 x-coordinate of the first system
     * @param y1 y-coordinate of the first system
     * @param z1 z-coordinate of the first system
     * @param x2 x-coordinate of the second system
     * @param y2 y-coordinate of the second system
     * @param z2 z-coordinate of the second system
     * @return distance in light years
     */
    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }


    /**
     * Calculates the great-circle distance between two points on a planet's surface using the haversine formula.
     *
     * @param lat1   latitude of the first point in degrees
     * @param lon1   longitude of the first point in degrees
     * @param lat2   latitude of the second point in degrees
     * @param lon2   longitude of the second point in degrees
     * @param radius planet radius in meters
     * @return distance in meters
     */
    public static double calculateSurfaceDistance(double lat1, double lon1, double lat2, double lon2, double radius) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance = radius * angular distance
        return radius * c;
    }


    /**
     * Formats a distance value in meters to a human-readable string representation.
     * If the distance is 1000 meters or greater, it is formatted in kilometers with one decimal place.
     * Otherwise, the distance is formatted in meters without decimal places.
     *
     * @param d the distance in meters to be formatted
     * @return a formatted string representing the distance in meters or kilometers
     */
    private static String formatDistance(double d) {
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
            return "Heading to target: " + bearingToTarget + " Your Heading " + userHeading + " Distance: " + distanceToTarget + " Speed: " + userSpeed + " Altitude: " + altitude;
        }
    }
}
