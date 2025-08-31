package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.GrokAnalysisEndpoint;

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
        GrokAnalysisEndpoint grokAnalysisEndpoint = GrokAnalysisEndpoint.getInstance();
        JsonObject analysis = grokAnalysisEndpoint.analyzeData(originalUserInput, dataJsonStr);
        // Ensure response_text is present
        if (!analysis.has("response_text")) {
            analysis = GenericResponse.getInstance().genericResponse("Analysis incomplete");
        }
        return analysis;
    }

}
