package elite.companion.data;

/**
 * Represents details for a Power Play power, including their capital, allegiance, and ethoses.
 * This is static data from the Elite Dangerous wiki.
 */
public class PowerDetails {
    private final String capital;
    private final String allegiance;
    private final String preparationEthos;
    private final String expansionEthos; // Note: Labeled as "extension ethos" in source data
    private final String controlEthos;

    public PowerDetails(String capital, String allegiance, String preparationEthos,
                        String expansionEthos, String controlEthos) {
        this.capital = capital;
        this.allegiance = allegiance;
        this.preparationEthos = preparationEthos;
        this.expansionEthos = expansionEthos;
        this.controlEthos = controlEthos;
    }

    // Getters
    public String getCapital() {
        return capital;
    }

    public String getAllegiance() {
        return allegiance;
    }

    public String getPreparationEthos() {
        return preparationEthos;
    }

    public String getExpansionEthos() {
        return expansionEthos;
    }

    public String getControlEthos() {
        return controlEthos;
    }

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