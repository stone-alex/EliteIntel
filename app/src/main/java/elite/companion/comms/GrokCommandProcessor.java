package elite.companion.comms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.session.SessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrokCommandProcessor {

    private static final Logger log = LoggerFactory.getLogger(GrokCommandProcessor.class);
    private static final GrokCommandProcessor INSTANCE = new GrokCommandProcessor();

    public static GrokCommandProcessor getInstance() {
        return INSTANCE;
    }

    private GrokCommandProcessor() {
        //NOTE: do not let anyone instantiate this class
    }


    protected void processResponse(String apiResponse) {
        try {
            JsonObject json = JsonParser.parseString(apiResponse).getAsJsonObject();

            String type = json.has("type") ? json.get("type").getAsString() : "";
            String responseText = json.has("response_text") ? json.get("response_text").getAsString() : "";

            switch (type.toLowerCase()) {
                case "command":
                    handleCommand(json, responseText);
                    break;

                case "query":
                    handleQuery(responseText);
                    break;

                case "chat":
                    handleChat(responseText);
                    break;

                default:
                    log.warn("Unknown response type: {}", type);
                    handleChat("Hmm, not sure about that one.");
            }
        } catch (Exception e) {
            log.error("Failed to parse Grok response: {}", e.getMessage());
            handleChat("Error processing command.");
        }
    }

    private void handleQuery(String responseText) {
        VoiceGenerator.getInstance().speak(responseText);
    }


    private void handleCommand(JsonObject json, String responseText) {
        String action = json.has("action") ? json.get("action").getAsString() : "";
        JsonObject params = json.has("params") ? json.get("params").getAsJsonObject() : new JsonObject();

        SessionTracker.getInstance().updateSession("action", action);
        SessionTracker.getInstance().updateSession("params", params.getAsJsonObject());
        handleChat(responseText);
    }

    private void handleChat(String responseText) {
        VoiceGenerator.getInstance().speak(responseText); // Direct TTS response
    }

}
