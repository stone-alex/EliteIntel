package elite.intel.util;

public class DistanceCalculator {
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
}