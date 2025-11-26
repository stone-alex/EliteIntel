package elite.intel.ai.brain;


public interface AiPromptFactory {
    //String generateSystemInstructions(String sensorInput);
    String generatePlayerInstructions(String playerVoiceInput);
    String generateAnalysisPrompt(String userIntent, String instructions);
    String generateSystemPrompt();
    String generateQueryPrompt();
    String appendBehavior();
}
