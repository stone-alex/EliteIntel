package elite.intel.ai.brain;


public interface AiPromptFactory {
    String generateUserPrompt(String playerVoiceInput);
    String generateAnalysisPrompt(String userIntent, String instructions);
    String generateSystemPrompt();
    String generateQueryPrompt();
    String appendBehavior();
    String generateSensorPrompt();
}
