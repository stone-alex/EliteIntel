package elite.companion.util;

public enum AICadence {
    IMPERIAL("Use British Cadence."),
    FEDERATION("Use American Cadence."),
    ALLIANCE("Use Eastern European Cadence.")
    ;

    private final String cadenceClause;

    AICadence(String behaviorClause) {
        this.cadenceClause = behaviorClause;
    }

    public String getCadenceClause() {
        return cadenceClause;
    }

}
