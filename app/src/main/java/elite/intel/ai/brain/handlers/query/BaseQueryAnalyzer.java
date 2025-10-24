package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

public class BaseQueryAnalyzer {

    protected Queries findQuery(String action) {
        for (Queries qa : Queries.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        throw new IllegalArgumentException("No query action found for: " + action);
    }

    protected JsonObject process(AiData data, String originalUserInput) {

        AiAnalysisInterface aiAnalysisInterface = ApiFactory.getInstance().getAnalysisEndpoint();
        JsonObject analysis = aiAnalysisInterface.analyzeData(originalUserInput, data);

        if (!analysis.has(AIConstants.PROPERTY_RESPONSE_TEXT)) {
            analysis = GenericResponse.getInstance().genericResponse("Analysis incomplete");
        }
        return analysis;
    }

    protected JsonObject process(String message) {
        EventBusManager.publish(new AiVoxResponseEvent(message));
        return new JsonObject();
    }

    protected Gson getGson() {
        return GsonFactory.getGson();
    }

    protected String toJson(Object obj) {
        return getGson().toJson(obj);
    }
}
