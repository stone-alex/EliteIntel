package elite.companion.comms.brain;

/**
 * This enum defines different cadences that an AI system can adopt.
 * Cadence specifies linguistic preferences, focusing on regional variations such as British or American English.
 */
public enum AICadence {
    IMPERIAL("Use British Cadence. Avoid American slang like dude."),
    FEDERATION("Use American Cadence. Avoid British slang like bloody."),
    ALLIANCE("Mix British and American cadence.");

    private final String cadenceClause;

    AICadence(String cadenceClause) {
        this.cadenceClause = cadenceClause;
    }

    public String getCadenceClause() {
        return cadenceClause;
    }
}