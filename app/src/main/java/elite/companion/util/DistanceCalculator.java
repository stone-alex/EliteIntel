package elite.companion.util;

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
}