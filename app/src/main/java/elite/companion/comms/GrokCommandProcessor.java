package elite.companion.comms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.EventBusManager;
import elite.companion.events.VoiceCommandDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public class GrokCommandProcessor {

    private static final Logger log = LoggerFactory.getLogger(GrokCommandProcessor.class);
    private static final GrokCommandProcessor INSTANCE = new GrokCommandProcessor();

    public static GrokCommandProcessor getInstance() {
        return INSTANCE;
    }

    private GrokCommandProcessor() {
        //NOTE: do not let anyone instantiate this class
    }


    protected void processResponse(String transcribedText, String apiResponse) {
        try {
            JsonObject json = JsonParser.parseString(apiResponse).getAsJsonObject();

            String type = json.has("type") ? json.get("type").getAsString() : "";
            String responseText = json.has("response_text") ? json.get("response_text").getAsString() : "";

            switch (type.toLowerCase()) {
                case "command":
                    handleCommand(transcribedText, json, responseText);
                    break;

                case "query":
                    //Handle the query


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


    private void handleCommand(String transcribedText, JsonObject json, String responseText) {
        String action = json.has("action") ? json.get("action").getAsString() : "";
        JsonObject params = json.has("params") ? json.get("params").getAsJsonObject() : new JsonObject();

        VoiceCommandDTO dto = new VoiceCommandDTO(Instant.now().toString(), transcribedText);
        dto.setInterpretedAction(action);
        // Set params generically (extend VoiceCommandDTO if needed for more fields)
        for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
            if (entry.getKey().equals("target")) {
                dto.setTarget(entry.getValue().getAsString());
            } // Add more setters for other param types as needed
        }
        EventBusManager.publish(dto);
        handleChat(responseText);
    }

    private void handleChat(String responseText) {
        VoiceNotifier.getInstance().speak(responseText); // Direct TTS response
    }

}
