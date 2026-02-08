package elite.intel.util;

public class LocationUtils {

    public static final double STANDARD_EARTH_GRAVITY = 9.80665;
    public static final double EARTH_RADIUS = 6371.0;

    public static double gravityFix(double massEM, double radius) {
        return (STANDARD_EARTH_GRAVITY * massEM / Math.pow(radius / EARTH_RADIUS, 2)) / STANDARD_EARTH_GRAVITY;
    }
}
