package elite.intel.util;

public class NavigationUtils {

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
    public static String getHeading(double entryLatitude, double entryLongitude,
                                    double userLatitude, double userLongitude,
                                    double planetRadius) {
        // Convert degrees to radians for trigonometric calculations
        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(entryLatitude);
        double lon2 = Math.toRadians(entryLongitude);

        // Calculate the difference in longitude
        double deltaLon = lon2 - lon1;

        // Calculate the initial bearing using spherical trigonometry
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);

        // Convert bearing from radians to degrees and normalize to 0-360
        double bearingDegrees = Math.toDegrees(bearing);
        bearingDegrees = (bearingDegrees + 360) % 360;
        int roundedBearing = (int) Math.round(bearingDegrees);

        // Calculate great-circle distance using the haversine formula
        // Convert planetRadius from meters to kilometers for distance calculation
        double planetRadiusKm = planetRadius / 1000.0;
        double deltaLat = lat2 - lat1;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = planetRadiusKm * c; // Distance in kilometers

        // Format distance for vocalization
        String distanceStr;
        if (distanceKm < 1.0) {
            // Convert to meters for distances under 1 km
            int distanceMeters = (int) Math.round(distanceKm * 1000);
            distanceStr = distanceMeters + " meters";
        } else {
            // Use kilometers with one decimal place for distances 1 km or greater
            double distanceKmRounded = Math.round(distanceKm * 10) / 10.0;
            distanceStr = String.format("%.1f", distanceKmRounded) + " kilometers";
        }

        // Return vocalization-friendly string
        return "Heading " + roundedBearing + " degrees, distance to target is " + distanceStr;
    }
}