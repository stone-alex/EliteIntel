package elite.intel.ai.brain;


public interface AiPromptFactory {
    String generateAnalysisPrompt();
    String generateVoiceInputSystemPrompt();
    String appendBehavior();
    String generateSensorPrompt();
}
