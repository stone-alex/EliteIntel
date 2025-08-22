package elite.companion.comms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.EventBusManager;
import elite.companion.events.VoiceCommandDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Scanner;

/**
 * TODO: This class must be refactored to be more generic. It currently only supports setting mining targets.
 * TODO: The user may issue various commands. But this can also include idle chat interaction with Grok AI.
 * For example the user may ask Grok How much money we have on our Carrier account. Grok will respond with the current balance. the balance can be obtained from the session tracker.
 * Or Grok may answer that it does not know, if the session tracker does not have that information yet.
 * */
public class GrokCommandProcessor {

    private static final Logger log = LoggerFactory.getLogger(GrokCommandProcessor.class);

    //TODO: this has tobe refactored to be much more generic. At the moment it only supports setting mining targets.
    public void processCommand(String transcribedText) {
        String prompt = String.format("Interpret this Elite Dangerous app command: '%s'. Output JSON: {'action': 'set_mining_target', 'target': 'Tritium'} or similar.", transcribedText);
        String response = callXaiApi(prompt);

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        String target = json.has("target") ? json.get("target").getAsString() : null;

        VoiceCommandDTO dto = new VoiceCommandDTO(Instant.now().toString(), transcribedText);
        dto.setInterpretedAction(action);
        if (target != null) dto.setTarget(target);
        EventBusManager.publish(dto);
    }

    private String callXaiApi(String prompt) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            String body = "{\"model\": \"grok-4\",  \"temperature\": 0.7,  \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            try (var os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            if (conn.getResponseCode() == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    return json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .get("message").getAsJsonObject()
                            .get("content").getAsString();
                }
            } else {
                log.error("xAI API error: {}", conn.getResponseCode());
                log.info(conn.getResponseMessage());
                return "{}";
            }
        } catch (Exception e) {
            log.error("AI API call fatal error: {}", e.getMessage());
            return "{}";
        }
    }

    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.x.ai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        //conn.setRequestProperty("Authorization", "Bearer " + Globals.readConfigFile(Globals.XAI_API_KEY, "key"));
        conn.setRequestProperty("Authorization", "Bearer " + "xai-0HNV4L5hmJQDdDuRRJXka8zwM8pFYS5HrY2MZEx39uwsSLDNpgivBFImXBGmiO1XaSgyWwowOOafVbVj");
        conn.setDoOutput(true);
        return conn;
    }
}
