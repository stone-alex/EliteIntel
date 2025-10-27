package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseQueryAnalyzer {

    private static final Logger log = LogManager.getLogger(BaseQueryAnalyzer.class);

    protected Queries findQuery(String action) {
        for (Queries qa : Queries.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        throw new IllegalArgumentException("No query action found for: " + action);
    }

    protected JsonObject process(AiData struct, String originalUserInput) {

        log.info("Processing data: \n\n{}\n\n", struct.getData().toJson());

        AiAnalysisInterface aiAnalysisInterface = ApiFactory.getInstance().getAnalysisEndpoint();
        JsonObject analysis = aiAnalysisInterface.analyzeData(originalUserInput, struct);

        if (!analysis.has(AIConstants.PROPERTY_RESPONSE_TEXT)) {
            analysis = GenericResponse.getInstance().genericResponse("Analysis incomplete");
        }
        return analysis;
    }

    protected JsonObject process(String message) {
        JsonObject object = new JsonObject();
        object.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, message);
        object.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, false);
        return object;
    }

    protected Gson getGson() {
        return GsonFactory.getGson();
    }

    protected String toJson(Object obj) {
        return getGson().toJson(obj);
    }
}
