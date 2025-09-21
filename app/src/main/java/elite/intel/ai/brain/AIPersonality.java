package elite.intel.ai.brain;

/**
 * Enum AIPersonality provides predefined personality traits that an AI system
 * can adopt to shape its response style and tone. Each personality type is
 * associated with a specific behavior clause that guides the AI's manner of communication.
 * <p>
 * Each personality represents a distinct communication style:
 * - PROFESSIONAL: Simulates a military professional with extremely concise and formal responses.
 * - FRIENDLY: Emulates a casual and approachable tone with friendly and informal responses.
 * - UNHINGED: Exhibits playful and humorous energy using light sarcasm and informal slang.
 * - ROGUE: Demonstrates bold and outspoken communication with heavy use of jargon and wit.
 * <p>
 * This enum is intended to be used in scenarios where the AI's tone and style
 * of interaction need to match specific contextual or user preferences.
 */
public enum AIPersonality {
    PROFESSIONAL("Respond extremely briefly and concisely as a military professional.", 0.1f),
    CASUAL("Respond extremely briefly and concisely in a casual tone like a colleague. Use occasional slang matching cadence.", 0.5f),
    FRIENDLY("Respond extremely briefly and concisely in a friendly, casual tone like a close friend. Use slang matching cadence.", 0.5f),
    UNHINGED("Respond extremely briefly  and concisely with playful, cheeky energy, emphasizing humor and light sarcasm. Use jargon and slang that matches cadence.", 0.8f),
    ROGUE("Respond extremely briefly and concisely with bold, in-your-face energy, using heavy jargon but staying sharp and witty within the selected cadence.", 1.0f);

    private final String behaviorClause;
    private float temperature = 0.5f;

    AIPersonality(String behaviorClause, float temperature) {
        this.behaviorClause = behaviorClause;
        this.temperature = temperature;
    }

    public float getTemperature() {
        return temperature;
    }

    public String getBehaviorClause() {
        return behaviorClause;
    }
}