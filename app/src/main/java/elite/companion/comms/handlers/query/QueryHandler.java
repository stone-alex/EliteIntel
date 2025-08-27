package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;

public interface QueryHandler {
    /**
     * Handles the query by fetching data from sessions or journal-derived memory.
     *
     * @param params JSON params from Grok (e.g., {"system": "Sol"}).
     * @return String representation of the data (e.g., "{\"population\": 20000000, \"economy\": \"High Tech\"}").
     * @throws Exception if data fetch fails.
     */
    String handle(String action, JsonObject params, String originalUserInput) throws Exception;
}