package elite.intel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for converting planetary mass (in Earth masses) to surface gravity (in Earth gravities)
 * and validating journal-provided gravity values.
 */
public class GravityCalculator {
    // Gravitational constant (m³ kg⁻¹ s⁻²)
    private static final BigDecimal G = new BigDecimal("6.67430E-11");
    // Earth's mass (kg)
    private static final BigDecimal EARTH_MASS = new BigDecimal("5.972E24");
    // Earth's surface gravity (m/s²)
    private static final BigDecimal EARTH_GRAVITY = new BigDecimal("9.80665");
    // Threshold for detecting journal gravity error (factor of ~9.8)
    private static final BigDecimal GRAVITY_ERROR_FACTOR = new BigDecimal("9.0");

    /**
     * Converts mass (in Earth masses) and radius (in meters) to surface gravity (in Earth gravities).
     *
     * @param massEM   Mass of the body in Earth masses.
     * @param radiusM  Radius of the body in meters.
     * @return Surface gravity in Earth gravities (g), or null if inputs are invalid.
     */
    public static Double calculateSurfaceGravity(double massEM, double radiusM) {
        if (massEM <= 0 || radiusM <= 0) {
            return null; // Invalid inputs
        }

        // Convert inputs to BigDecimal for precise calculations
        BigDecimal massEarthMasses = new BigDecimal(String.valueOf(massEM));
        BigDecimal radiusMeters = new BigDecimal(String.valueOf(radiusM));

        // Convert mass to kilograms
        BigDecimal massKg = massEarthMasses.multiply(EARTH_MASS);

        // Calculate surface gravity: g = G * M / R²
        BigDecimal radiusSquared = radiusMeters.multiply(radiusMeters);
        BigDecimal gravity = G.multiply(massKg).divide(radiusSquared, 10, RoundingMode.HALF_UP);

        // Convert to Earth gravities
        BigDecimal relativeGravity = gravity.divide(EARTH_GRAVITY, 4, RoundingMode.HALF_UP);

        return relativeGravity.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}