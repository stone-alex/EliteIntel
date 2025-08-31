package elite.companion.data;

import java.util.HashMap;
import java.util.Map;

public class PowerPlayData {

    private static final Map<String, PowerDetails> POWER_MAP = new HashMap<>();

    static {
        // Populate from wiki data (trimmed and cleaned)
        POWER_MAP.put("aisling duval", new PowerDetails("Cubeo", "Empire", "Social", "Finance", "Social"));
        POWER_MAP.put("archon delaine", new PowerDetails("Harma", "Independent", "Combat", "Combat", "Social"));
        POWER_MAP.put("a. lavigny-duval", new PowerDetails("Kamadhenu", "Empire", "Social", "Combat", "Combat"));
        POWER_MAP.put("denton patreus", new PowerDetails("Eotienses", "Empire", "Finance", "Combat", "Combat"));
        POWER_MAP.put("edmund mahon", new PowerDetails("Gateway", "Alliance", "Finance", "Finance", "Combat"));
        POWER_MAP.put("felicia winters", new PowerDetails("Rhea", "Federation", "Social", "Finance", "Finance"));
        POWER_MAP.put("jerome archer", new PowerDetails("Nanomam", "Federation", "Combat", "Combat", "Combat"));
        POWER_MAP.put("li yong-rui", new PowerDetails("Lembava", "Independent", "Social", "Finance", "Finance"));
        POWER_MAP.put("nakato kaine", new PowerDetails("Tionisla", "Alliance", "Social", "Covert", "Social"));
        POWER_MAP.put("pranav antal", new PowerDetails("Polevnic", "Independent", "Social", "Social", "Covert"));
        POWER_MAP.put("yuri grom", new PowerDetails("Clayakarma", "Independent", "Covert", "Combat", "Covert"));
        POWER_MAP.put("zemina torval", new PowerDetails("Synteini", "Empire", "Finance", "Finance", "Covert"));
    }

    /**
     * Retrieve details for a power by name (case-insensitive).
     * @param powerName The name of the power (e.g., "Aisling Duval").
     * @return PowerDetails if found, or null if not.
     */
    public static PowerDetails getPowerDetails(String powerName) {
        return POWER_MAP.get(powerName.toLowerCase().trim());
    }

    /**
     * Check if a power exists in the data.
     * @param powerName The name of the power (case-insensitive).
     * @return true if the power is known.
     */
    public static boolean hasPower(String powerName) {
        return POWER_MAP.containsKey(powerName.toLowerCase().trim());
    }

    /**
     * Get all powers as a map for iteration or full export.
     * @return Unmodifiable view of the power map.
     */
    public static Map<String, PowerDetails> getAllPowers() {
        return Map.copyOf(POWER_MAP);
    }
}
