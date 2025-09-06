package elite.companion.comms.brain;

import com.google.gson.JsonObject;

/**
 * Provides a contract for analyzing data based on user intent and raw data input.
 * The implementation of this interface is expected to perform domain-specific
 * data analysis and return the resulting output within a structured JSON format.
 */
public interface AiAnalysisInterface {
    JsonObject analyzeData(String userIntent, String dataJson);
}
