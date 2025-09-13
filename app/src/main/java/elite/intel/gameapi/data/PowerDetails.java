package elite.intel.gameapi.data;

/**
 * Represents details for a Power Play power, including their capital, allegiance, and ethoses.
 * This is static data from the Elite Dangerous wiki.
 *
 * @param expansionEthos Note: Labeled as "extension ethos" in source data
 */
public record PowerDetails(String capital, String allegiance, String preparationEthos, String expansionEthos, String controlEthos) {

    @Override
    public String toString() {
        return "PowerDetails{" +
                "capital='" + capital + '\'' +
                ", allegiance='" + allegiance + '\'' +
                ", preparationEthos='" + preparationEthos + '\'' +
                ", expansionEthos='" + expansionEthos + '\'' +
                ", controlEthos='" + controlEthos + '\'' +
                '}';
    }
}