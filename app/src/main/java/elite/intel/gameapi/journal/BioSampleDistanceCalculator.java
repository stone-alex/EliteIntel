package elite.intel.gameapi.journal;

import elite.intel.gameapi.data.BioForms;
import elite.intel.util.BioScanDistances;
import elite.intel.util.DistanceCalculator;

import java.util.Map;

public class BioSampleDistanceCalculator {


    public static boolean isFarEnoughFromSample(String genus, String species, double scanLat, double scanLong, double positionLat, double positionLong, double planetRadius) {
        BioForms.BioDetails details = BioForms.getDetails(genus, species);      // primary data
        Map<String, Double> genusDistanceMap = BioScanDistances.GENUS_TO_CCR;   //fall back
        double requiredDistance = details == null ? genusDistanceMap.get(genus) : details.colonyRange();
        double distanceFromSample = DistanceCalculator.calculateSurfaceDistance(scanLat, scanLong, positionLat, positionLong, planetRadius);
        return distanceFromSample > requiredDistance;
    }
}
