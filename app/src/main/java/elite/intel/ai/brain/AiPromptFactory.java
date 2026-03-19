package elite.intel.ai.brain;


public interface AiPromptFactory {
    String generateAnalysisPrompt();

    String generateUserInputSystemPrompt();
    String appendBehavior();
    String generateSensorPrompt();
}
