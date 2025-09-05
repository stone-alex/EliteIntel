package elite.companion.comms.ai;

public enum AIPersonality {
    PROFESSIONAL("Respond extremely briefly and concisely as a military professional."),
    FRIENDLY("Respond extremely briefly and concisely in a friendly, casual tone like a close friend. Use slang matching cadence."),
    UNHINGED("Respond extremely briefly  and concisely with playful, cheeky energy, emphasizing humor and light sarcasm. Use jargon and slang that matches cadence."),
    ROGUE("Respond extremely briefly and concisely with bold, in-your-face energy, using heavy jargon but staying sharp and witty within the selected cadence.");

    private final String behaviorClause;

    AIPersonality(String behaviorClause) {
        this.behaviorClause = behaviorClause;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}