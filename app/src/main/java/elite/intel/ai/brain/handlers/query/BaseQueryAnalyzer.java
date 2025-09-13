package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AiAnalysisInterface;

/**
 * The BaseQueryAnalyzer class provides core functionality for analyzing queries and processing data.
 * It serves as a foundational class designed to be extended by specific query handlers.
 * <p>
 * Core responsibilities include:
 * - Identifying and matching query actions based on provided action strings.
 * - Performing data analysis using an external AI analysis interface and ensuring the response
 * adheres to the expected structure.
 * <p>
 * Implementing classes can utilize the utilities provided by BaseQueryAnalyzer to standardize
 * query handling and analysis processes.
 * <p>
 * Methods:
 * - findQuery: Matches a specific action string to its corresponding QueryActions enumeration.
 * - analyzeData: Sends given data for analysis via an AI interface and validates the response structure.
 * <p>
 * This class assumes the existence of:
 * - An enumeration `QueryActions` that defines various supported actions and their properties.
 * - An implementation of the `AiAnalysisInterface` for data analysis.
 */
public class BaseQueryAnalyzer {

    protected QueryActions findQuery(String action) {
        for (QueryActions qa : QueryActions.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        throw new IllegalArgumentException("No query action found for: " + action);
    }


    protected JsonObject analyzeData(String dataJsonStr, String originalUserInput) {
        // For analysis-needed queries
        AiAnalysisInterface aiAnalysisInterface = ApiFactory.getInstance().getAnalysisEndpoint();
        JsonObject analysis = aiAnalysisInterface.analyzeData(originalUserInput, dataJsonStr);
        // Ensure response_text is present
        if (!analysis.has("response_text")) {
            analysis = GenericResponse.getInstance().genericResponse("Analysis incomplete");
        }
        return analysis;
    }

}
