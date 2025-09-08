package elite.companion.ai.search.api;

import com.google.gson.JsonElement;
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

public class EdsmApiClient {
    private static final Logger log = LoggerFactory.getLogger(EdsmApiClient.class);
    private static final String BASE_URL = "https://www.edsm.net";

    private static final String API_KEY = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_EDSM_KEY);

    private static StringBuilder authenticatedUrl(String endpoint) {
        return new StringBuilder(BASE_URL + endpoint + "?apiKey=" + API_KEY);
    }

    public static JsonObject searchStarSystem(String starSystemName, int showInformation) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-v1/system";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        query.append("&showInformation=").append(showInformation);
        return query(query);
    }

    public static JsonObject searchFaction(String starSystemName, int showHistory) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/factions";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        query.append("&showInformation=").append(showHistory);
        return query(query);
    }


    public static JsonObject searchSystemBodies(String starSystemName) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/bodies";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        return query(query);
    }

    public static JsonObject searchTraffic(String starSystemName) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/traffic";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        return query(query);
    }

    public static JsonObject searchDeaths(String starSystemName) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/deaths";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        return query(query);
    }


    public static JsonObject searchEstimatedScanValues(String starSystemName) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/estimated-value";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        return query(query);
    }

    public static JsonObject searchStations(String starSystemName) {
        if (starSystemName == null) return new JsonObject();
        String endpoint = "/api-system-v1/stations";
        StringBuilder query = authenticatedUrl(endpoint);
        query.append("&systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        return query(query);
    }

    public static JsonObject searchMarket(String marketId, String orSystemName, String andStationName) {
        if (marketId == null && orSystemName == null && andStationName == null) return new JsonObject();

        String endpoint = "/api-system-v1/stations/market";
        return servicesSearch(marketId, orSystemName, andStationName, endpoint);
    }

    public static JsonObject searchShipyard(String marketId, String orSystemName, String andStationName) {
        if (marketId == null && orSystemName == null && andStationName == null) return new JsonObject();
        String endpoint = "/api-system-v1/stations/shipyard";
        return servicesSearch(marketId, orSystemName, andStationName, endpoint);
    }


    public static JsonObject searchOutfitting(String marketId, String orSystemName, String andStationName) {
        if (marketId == null && orSystemName == null && andStationName == null) return new JsonObject();
        String endpoint = "/api-system-v1/stations/outfitting";
        return servicesSearch(marketId, orSystemName, andStationName, endpoint);
    }


    private static JsonObject servicesSearch(String marketId, String orSystemName, String andStationName, String endpoint) {
        StringBuilder query = authenticatedUrl(endpoint);
        if (marketId != null && !marketId.isEmpty()) {
            query.append("&marketId=").append(URLEncoder.encode(marketId, StandardCharsets.UTF_8));
        } else {
            query.append("&systemName=").append(URLEncoder.encode(orSystemName, StandardCharsets.UTF_8));
            query.append("&stationName=").append(URLEncoder.encode(andStationName, StandardCharsets.UTF_8));
        }
        return query(query);
    }


    private static JsonObject query(StringBuilder query) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(query.toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("EDSM API error: {} - {}", responseCode, conn.getResponseMessage());
                return new JsonObject();
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                log.debug("EDSM API response: {}", response);
                JsonElement jsonElement = JsonParser.parseString(response);

                JsonObject wrapper = new JsonObject();
                wrapper.add("data", jsonElement);
                wrapper.addProperty("timestamp", System.currentTimeMillis());
                return wrapper;
            }
        } catch (Exception e) {
            log.error("Failed to query EDSM API: {}", e.getMessage(), e);
            return new JsonObject();
        }
    }
}