package elite.intel.gameapi.data;

import java.util.HashMap;
import java.util.Map;

public class PowerPlayData {

    private static final Map<String, PowerDetails> POWER_MAP = new HashMap<>();
    private static final Map<String, String> JOURNAL_TO_WIKI = new HashMap<>();


    static {
        // Wiki keys (your original data, lowercase full names)
        POWER_MAP.put("aisling duval", new PowerDetails("Cubeo", "Empire", "Social", "Finance", "Social"));
        POWER_MAP.put("archon delaine", new PowerDetails("Harma", "Independent", "Combat", "Combat", "Social"));
        POWER_MAP.put("arissa lavigny-duval", new PowerDetails("Kamadhenu", "Empire", "Social", "Combat", "Combat"));
        POWER_MAP.put("denton patreus", new PowerDetails("Eotienses", "Empire", "Finance", "Combat", "Combat"));
        POWER_MAP.put("edmund mahon", new PowerDetails("Gateway", "Alliance", "Finance", "Finance", "Combat"));
        POWER_MAP.put("felicia winters", new PowerDetails("Rhea", "Federation", "Social", "Finance", "Finance"));
        POWER_MAP.put("jerome archer", new PowerDetails("Nanomam", "Federation", "Combat", "Combat", "Combat"));
        POWER_MAP.put("li yong-rui", new PowerDetails("Lembava", "Independent", "Social", "Finance", "Finance"));
        POWER_MAP.put("nakato kaine", new PowerDetails("Tionisla", "Alliance", "Social", "Covert", "Social"));
        POWER_MAP.put("pranav antal", new PowerDetails("Polevnic", "Independent", "Social", "Social", "Covert"));
        POWER_MAP.put("yuri grom", new PowerDetails("Clayakarma", "Independent", "Covert", "Combat", "Covert"));
        POWER_MAP.put("zemina torval", new PowerDetails("Synteini", "Empire", "Finance", "Finance", "Covert"));

        // Journal-to-wiki mapping (journal names normalized to lowercase)
        JOURNAL_TO_WIKI.put("aisling duval", "aisling duval");
        JOURNAL_TO_WIKI.put("archon delaine", "archon delaine");
        JOURNAL_TO_WIKI.put("a. lavigny-duval", "arissa lavigny-duval");
        JOURNAL_TO_WIKI.put("d. patreus", "denton patreus");
        JOURNAL_TO_WIKI.put("e. mahon", "edmund mahon");
        JOURNAL_TO_WIKI.put("f. winters", "felicia winters");
        JOURNAL_TO_WIKI.put("j. archer", "jerome archer");
        JOURNAL_TO_WIKI.put("li yong-rui", "li yong-rui");
        JOURNAL_TO_WIKI.put("n. kaine", "nakato kaine");
        JOURNAL_TO_WIKI.put("pranav antal", "pranav antal");
        JOURNAL_TO_WIKI.put("y. grom", "yuri grom");
        JOURNAL_TO_WIKI.put("z. torval", "zemina torval");
    }


    /**
     * Retrieve details for a power by name (case-insensitive), supporting journal abbreviations.
     *
     * @param powerName The name of the power (e.g., "A. Lavigny-Duval" from journal).
     * @return PowerDetails if found, or null if not.
     */
    public static PowerDetails getPowerDetails(String powerName) {
        String normalized = powerName.toLowerCase().trim();
        String wikiKey = JOURNAL_TO_WIKI.getOrDefault(normalized, normalized); // Fallback to direct if no mapping
        return POWER_MAP.get(wikiKey);
    }

    /**
     * Check if a power exists in the data.
     *
     * @param powerName The name of the power (case-insensitive).
     * @return true if the power is known.
     */
    public static boolean hasPower(String powerName) {
        if(powerName == null || powerName.isEmpty()) return false;
        return POWER_MAP.containsKey(powerName.toLowerCase().trim());
    }

    /**
     * Get all powers as a map for iteration or full export.
     *
     * @return Unmodifiable view of the power map.
     */
    public static Map<String, PowerDetails> getAllPowers() {
        return Map.copyOf(POWER_MAP);
    }
}
