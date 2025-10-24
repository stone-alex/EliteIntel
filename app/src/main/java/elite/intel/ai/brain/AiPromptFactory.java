package elite.intel.ai.brain;

import elite.intel.util.json.AiData;


public interface AiPromptFactory {
    String generateSystemInstructions(String sensorInput);
    String generatePlayerInstructions(String playerVoiceInput);
    String generateAnalysisPrompt(String userIntent, AiData data);
    String generateSystemPrompt();
    String generateQueryPrompt();
    void appendBehavior(StringBuilder sb);
}
