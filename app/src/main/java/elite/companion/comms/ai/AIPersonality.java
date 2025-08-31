package elite.companion.comms.ai;

public enum AIPersonality {
    PROFESSIONAL("Respond extremely briefly and concisely as a military professional."),
    FRIENDLY("Respond extremely briefly and concisely in a friendly, casual tone like a close friend. Use slang."),
    UNHINGED("Respond extremely briefly  and concisely with playful, cheeky energy, emphasizing humor and light sarcasm within the selected cadence. Use slang."),
    ROGUE("Respond extremely briefly  and concisely with bold, in-your-face energy inspired by George Carlin, staying sharp and witty within the selected cadence. avoid regional Earth-specific slang")
    ;

    private final String behaviorClause;

    AIPersonality(String behaviorClause) {
        this.behaviorClause = behaviorClause;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}