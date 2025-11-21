package elite.intel.gameapi.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Elite Dangerous Odyssey (2025) — Complete material storage caps
 * All categories use the same tier system:
 * G1=300, G2=250, G3=200, G4=150, G5=100
 */
public final class EDMaterialCaps {

    private static final int[] CAPS = {0, 300, 250, 200, 150, 100}; // index = grade

    private static final Map<String, Integer> GRADE_MAP = new HashMap<>();

    static {
        // ==================== RAW MATERIALS (Elements) ====================
        grade(1, "Carbon", "Iron", "Nickel", "Phosphorus", "Sulphur", "Lead", "Rhenium");
        grade(2, "Arsenic", "Chromium", "Germanium", "Manganese", "Vanadium", "Zinc", "Zirconium");
        grade(3, "Boron", "Cadmium", "Mercury", "Molybdenum", "Niobium", "Tin", "Tungsten");
        grade(4, "Antimony", "Polonium", "Ruthenium", "Selenium", "Technetium", "Tellurium", "Yttrium");

        // ==================== MANUFACTURED ====================
        // G1
        grade(1, "Tempered Alloys", "Basic Conductors", "Chemical Storage Units", "Salvaged Alloys", "Worn Shield Emitters",
                "Mechanical Scrap", "Heat Resistant Ceramics", "Conductive Components", "Shield Emitters", "Crystal Shards");
        // G2
        grade(2, "Heat Conduction Wiring", "Chemical Processors", "Compact Composites", "Galvanising Alloys", "Shielding Sensors",
                "Grid Resistors", "Heat Dispersion Plate", "Mechanical Components", "Filament Composites", "High Density Composites");
        // G3
        grade(3, "Heat Exchangers", "Chemical Distillery", "Conductive Ceramics", "Precipitated Alloys", "Military Grade Alloys",
                "Electrochemical Arrays", "Heat Vanes", "Mechanical Equipment", "Polymer Capacitors", "Thermic Alloys");
        // G4
        grade(4, "Focus Crystals", "Chemical Manipulators", "Conductive Polymers", "Pharmaceutical Isolators", "Hybrid Capacitors",
                "Imperial Shielding", "Core Dynamics Composites", "Military Supercapacitors", "Proprietary Composites", "Proto Light Alloys");
        // G5
        grade(5, "Refined Focus Crystals", "Exquisite Focus Crystals", "Biotech Conductors", "Proto Heat Radiators", "Proto Radiolic Alloys");

        // Special / Exotics
        grade(5, "Configurable Components", "Improvised Components", "Compound Shielding");
        grade(4, "Flawed Focus Crystals"); // G4 despite name

        // Thargoid
        grade(5, "Propulsion Elements", "Weapon Parts", "Wreckage Components", "Sensor Fragment", "Caustic Shard", "Caustic Crystal",
                "Hardened Surface Fragments", "Heat Exposure Specimen", "Phasing Membrane Residue", "Corrosive Mechanisms");

        // Guardian
        grade(4, "Guardian Power Cell", "Guardian Power Conduit", "Guardian Sentinel Weapon Parts",
                "Guardian Sentinel Wreckage Components", "Guardian Technology Component");

        // ==================== ENCODED / DATA ====================
        // G1
        grade(1, "Anomalous Bulk Scan Data", "Atypical Disrupted Wake Echoes", "Distorted Shield Cycle Recordings",
                "Inconsistent Shield Soak Analysis", "Untypical Shield Scans", "Unusual Encrypted Files");
        // G2
        grade(2, "Aberrant Shield Pattern Analysis", "Abnormal Compact Emission Data", "Irregular Emission Data",
                "Unexpected Emission Data", "Tagged Encryption Codes");
        // G3
        grade(3, "Adaptive Encryptors Capture", "Atypical Encryption Archives", "Decoded Emission Data", "Open Symmetric Keys",
                "Security Firmware Patch", "Modified Consumer Firmware", "Modified Embedded Firmware");
        // G4
        grade(4, "Anomalous FSD Telemetry", "Divergent Scan Data", "Eccentric Hyperspace Trajectories", "Strange Wake Solutions",
                "Datamined Wake Exceptions", "Classified Scan Databanks", "Unidentified Scan Archives");
        // G5
        grade(5, "Exceptional Scrambled Emission Data", "Cracked Industrial Firmware", "Specialised Legacy Firmware",
                "Classified Scan Fragment", "Peculiar Shield Frequency Data", "Massive Energy Surge Analytics");

        // Guardian Blueprints
        grade(5, "Guardian Module Blueprint Segment", "Guardian Vessel Blueprint Segment", "Guardian Weapon Blueprint Segment");

        // Thargoid Data
        grade(5, "Thargoid Interdiction Telemetry", "Thargoid Material Composition Data", "Thargoid Residue Data",
                "Thargoid Ship Signature", "Thargoid Structural Data", "Thargoid Wake Data");

        // Obelisk Data
        grade(4, "Pattern Alpha Obelisk Data", "Pattern Beta Obelisk Data", "Pattern Gamma Obelisk Data",
                "Pattern Delta Obelisk Data", "Pattern Epsilon Obelisk Data");
    }

    private static void grade(int grade, String... names) {
        for (String name : names) {
            GRADE_MAP.put(name, grade);
        }
    }

    /**
     * Returns the maximum allowed amount for any material.
     * Returns 0 if unknown (should never happen with this map).
     */
    public static int getMax(String materialName) {
        Integer grade = GRADE_MAP.get(materialName);
        if (grade == null) {
            return 300; // safe fallback
        }
        return CAPS[grade];
    }

}