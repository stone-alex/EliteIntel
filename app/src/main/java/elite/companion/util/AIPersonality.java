package elite.companion.util;

public enum AIPersonality {
    PROFESSIONAL("Respond briefly and concisely as a military professional."),
    FAMILIAR("Respond in a friendly, casual tone like a close friend."),
    UNHINGED("Respond with playful, cheeky energy, emphasizing humor and light sarcasm within the selected cadence."),
    ROGUE("Respond with bold, in-your-face energy inspired by George Carlin, staying sharp and witty within the selected cadence.")
    ;

    private final String behaviorClause;

    AIPersonality(String behaviorClause) {
        this.behaviorClause = behaviorClause;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}