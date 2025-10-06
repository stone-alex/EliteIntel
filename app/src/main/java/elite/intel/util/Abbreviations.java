package elite.intel.util;

import java.util.HashMap;
import java.util.Map;

public class Abbreviations {

    public static String generateAbbreviations() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported abbreviations:\n");
        Map<String, String> abbreviations = Abbreviations.getAbbreviations();
        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        sb.append(". ");
        return sb.toString();
    }


    private static Map<String, String> getAbbreviations() {
        Map<String, String> abbreviations = new HashMap<>();
        abbreviations.put("FSS", "Filtered Spectrum Scanner");
        abbreviations.put("DSS", "Detailed Surface Scanner");
        abbreviations.put("SRV", "(Surface Recon Vehicle)");
        abbreviations.put("FSD", "Frame Shift Drive");
        abbreviations.put("FTL", "Faster Than Light");
        abbreviations.put("Disco Scanner", "Discovery Scanner");
        abbreviations.put("SSS", "Strong Signal Source");
        abbreviations.put("USS", "Unidentified Signal Source");
        abbreviations.put("KLY", "Kilo Light Year");
        abbreviations.put("SOL", "Sol System. (where Earth is at. Center of galactic coordinate system)");
        return abbreviations;
    }
}
