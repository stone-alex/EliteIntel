package elite.companion.ai.search.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.ai.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EdsmApiClient {
    private static final Logger log = LoggerFactory.getLogger(EdsmApiClient.class);
    private static final String BASE_URL = "https://www.edsm.net/api-v1";

    private static final String API_KEY = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);


    private static final ConcurrentHashMap<String, JsonArray> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(30);

    public static JsonArray searchStations(String system, String service) {
        String cacheKey = system + ":" + service;
        if (cache.containsKey(cacheKey) && cache.get(cacheKey).getAsJsonObject().has("timestamp") &&
                System.currentTimeMillis() - cache.get(cacheKey).getAsJsonObject().get("timestamp").getAsLong() < CACHE_TTL) {
            log.debug("Returning cached EDSM response for key: {}", cacheKey);
            return cache.get(cacheKey);
        }

        try {
            String endpoint = "/stations";
            StringBuilder query = new StringBuilder(BASE_URL + endpoint + "?apiKey=" + API_KEY);
            if (system != null) query.append("&systemName=").append(URLEncoder.encode(system, StandardCharsets.UTF_8));
            if (service != null) query.append("&services=").append(service);

            HttpURLConnection conn = (HttpURLConnection) new URL(query.toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("EDSM API error: {} - {}", responseCode, conn.getResponseMessage());
                return new JsonArray();
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                log.debug("EDSM API response: {}", response);
                JsonArray result = JsonParser.parseString(response).getAsJsonArray();
                JsonObject wrapper = new JsonObject();
                wrapper.add("data", result);
                wrapper.addProperty("timestamp", System.currentTimeMillis());
                cache.put(cacheKey, wrapper.getAsJsonArray("data"));
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to query EDSM API: {}", e.getMessage(), e);
            return new JsonArray();
        }
    }
}