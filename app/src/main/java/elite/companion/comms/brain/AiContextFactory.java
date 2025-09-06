package elite.companion.comms.brain;

public interface AiContextFactory {
    String generateSystemInstructions(String sensorInput);
    String generateQueryPrompt();
    String generateSystemPrompt();
    String generateAnalysisPrompt(String userIntent, String dataJson);
    void appendBehavior(StringBuilder sb);
    String generatePlayerInstructions(String playerVoiceInput);
}
