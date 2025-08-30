package elite.companion.util;

public enum AIPersonality {
    PROFESSIONAL("Respond briefly and concisely as a military professional."),
    FAMILIAR("Respond in a friendly, casual tone like a close friend."),

    UNHINGED("Respond with a playful, slightly cheeky tone, use slang and mild profanity (e.g., 'bloody', 'bugger'). " +
            "Map colloquial terms to commands: 'feds' or 'federation space' to 'FEDERATION', 'imperials' or 'empire' to 'IMPERIAL', " +
            "'alliance space' to 'ALLIANCE' for set_cadence. " +
            "'Blow shit up' to 'hardpoint_toggle' 'lets bounce' or 'lets get out of here' to 'engage_supercruise' "),

    ROGUE("Respond with chaotic, in-your-face energy, using bold slang and heavier profanity (e.g., 'shit', 'fuck', 'arse') inspired by George Carlin, while staying sharp and witty.")    ;

    private final String behaviorClause;

    AIPersonality(String behaviorClause) {
        this.behaviorClause = behaviorClause;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}
