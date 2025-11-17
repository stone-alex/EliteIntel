package elite.intel.ai.search.spansh.station;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class StationSearchClient {

    private static StationSearchClient instance;


    private StationSearchClient() {
    }

    public static synchronized StationSearchClient getInstance() {
        if (instance == null) {
            instance = new StationSearchClient();
        }
        return instance;
    }

    private final Logger log = LogManager.getLogger(StationSearchClient.class);
    private final String BASE_URL = "https://spansh.co.uk/api/stations/search/save";
    private final String RESULTS_URL = "https://spansh.co.uk/api/stations/search/recall/";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = GsonFactory.getGson();


    public JsonObject performSearch(ToJsonConvertible criteria) throws IOException, InterruptedException {

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(criteria.toJson()))
                .build();


        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        if (postResponse.statusCode() != 200) {
            log.warn("POST to station search failed with status: {}", postResponse.statusCode());
            return null;
        }


        JsonObject postJson = gson.fromJson(postResponse.body(), JsonObject.class);
        String searchRefId = postJson.get("search_reference") != null ? postJson.get("search_reference").getAsString() : null;
        if (searchRefId == null || searchRefId.isEmpty()) {
            log.warn("Invalid or missing search reference ID in POST response");
            return null;
        }


        HttpRequest searchResultRequest = HttpRequest.newBuilder()
                .uri(URI.create(RESULTS_URL + searchRefId))
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                .header("Content-Type", "application/json;charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> searchResultResponse = httpClient.send(searchResultRequest, HttpResponse.BodyHandlers.ofString());
        if (searchResultResponse.statusCode() != 200) {
            log.warn("GET to results/{} failed with status: {}", searchRefId, searchResultResponse.statusCode());
            return null;
        }
        return gson.fromJson(searchResultResponse.body(), JsonObject.class);
    }
}
