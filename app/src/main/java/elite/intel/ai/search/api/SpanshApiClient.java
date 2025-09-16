package elite.intel.ai.search.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SpanshApiClient {
    private static final Logger log = LogManager.getLogger(SpanshApiClient.class);
    private static final String BASE_URL = "https://spansh.co.uk/api";

    public static JsonArray searchStations(String system, String service, String commodity, String module) {
        try {
            String endpoint = "/stations/search";
            StringBuilder query = new StringBuilder(BASE_URL + endpoint + "?");
            if (system != null) query.append("system=").append(URLEncoder.encode(system, StandardCharsets.UTF_8)).append("&");
            if (service != null) query.append("service=").append(service).append("&");
            if (commodity != null) query.append("commodity=").append(commodity).append("&");
            if (module != null) query.append("module=").append(module).append("&");
            query.append("radius=100"); // Default 100ly radius

            URI uri = URI.create(query.toString());
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("Spansh API error: {} - {}", responseCode, conn.getResponseMessage());
                return new JsonArray();
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                log.debug("Spansh API response: {}", response);
                return JsonParser.parseString(response).getAsJsonArray();
            }
        } catch (Exception e) {
            log.error("Failed to query Spansh API: {}", e.getMessage(), e);
            return new JsonArray();
        }
    }
}