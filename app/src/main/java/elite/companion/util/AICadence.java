package elite.companion.util;

public enum AICadence {
    IMPERIAL("Use British Cadence. Avoid American slang."),
    FEDERATION("Use American Cadence. Avoid British slang."),
    ALLIANCE("Mix British and American cadence.")
    ;

    private final String cadenceClause;

    AICadence(String cadenceClause) {
        this.cadenceClause = cadenceClause;
    }

    public String getCadenceClause() {
        return cadenceClause;
    }
}