package elite.companion.util;

public enum AICadence {
    IMPERIAL("Use a formal British tone with terms like 'mate,' 'guv,' 'bloody,' or 'cheers.' Avoid American slang."),
    FEDERATION("Use a confident American tone with terms like 'dude,' 'buddy,' 'awesome,' or 'y'all.' Avoid British slang."),
    ALLIANCE("Use a neutral, friendly spacer tone with terms like 'mate,' or 'friend'. Mix British and American accents but avoid regional Earth-specific slang.")
    ;

    private final String cadenceClause;

    AICadence(String cadenceClause) {
        this.cadenceClause = cadenceClause;
    }

    public String getCadenceClause() {
        return cadenceClause;
    }
}