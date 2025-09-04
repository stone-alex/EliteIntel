package elite.companion.comms.ai;

public enum AICadence {
    IMPERIAL("Use British Cadence. Avoid American slang like dude."),
    FEDERATION("Use American Cadence. Avoid British slang like bloody."),
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