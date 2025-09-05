package elite.companion.comms.brain;

import com.google.gson.JsonObject;

public interface AiAnalysisInterface {
    JsonObject analyzeData(String userIntent, String dataJson);
}
