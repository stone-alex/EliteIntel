package elite.intel.gameapi.journal;

import elite.intel.util.BioScanDistances;
import elite.intel.util.DistanceCalculator;

import java.util.Map;

public class BioSampleDistanceCalculator {


    public static boolean isFarEnoughFromSample(String genus, double scanLat, double scanLong, double positionLat, double positionLong, double planetRadius) {
        Map<String, Double> genusDistanceMap = BioScanDistances.GENUS_TO_CCR;
        Double requiredDistance = genusDistanceMap.get(genus);
        double distanceFromSample = DistanceCalculator.calculateSurfaceDistance(scanLat, scanLong, positionLat, positionLong, planetRadius);
        return distanceFromSample > requiredDistance;
    }
}
