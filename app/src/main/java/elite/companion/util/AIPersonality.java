package elite.companion.util;

public enum AIPersonality {
    PROFESSIONAL("Respond briefly and concisely as a military professional."),
    FAMILIAR("Respond in a friendly, casual tone like a close friend."),
    UNHINGED("Respond with a playful, slightly cheeky tone, use slang and profanity.")
    ;

    private final String behaviorClause;

    AIPersonality(String behaviorClause) {
        this.behaviorClause = behaviorClause;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}
