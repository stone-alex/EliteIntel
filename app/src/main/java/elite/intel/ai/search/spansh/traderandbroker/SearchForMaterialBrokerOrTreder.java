package elite.intel.ai.search.spansh.traderandbroker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SearchForMaterialBrokerOrTreder {

    private static final Logger log = LogManager.getLogger(SearchForMaterialBrokerOrTreder.class);
    private static final String BASE_URL = "https://spansh.co.uk/api/stations/search/save";
    private static final String RESULTS_URL = "https://spansh.co.uk/api/stations/search/recall/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = GsonFactory.getGson();

    public static List<TraderOrBrokerSearchDto.Result> findMaterialTrader(ToJsonConvertible searchCriteria) {

        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(searchCriteria.toJson()))
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
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .GET()
                    .build();

            HttpResponse<String> searchResultResponse = httpClient.send(searchResultRequest, HttpResponse.BodyHandlers.ofString());
            if (searchResultResponse.statusCode() != 200) {
                log.warn("GET to results/{} failed with status: {}", searchRefId, searchResultResponse.statusCode());
                return null;
            }

            JsonObject getJson = gson.fromJson(searchResultResponse.body(), JsonObject.class);
            TraderOrBrokerSearchDto dto = GsonFactory.getGson().fromJson(getJson, TraderOrBrokerSearchDto.class);
            return dto.getResults();
        } catch (Exception e) {
            log.error("Failed to find material trader", e);
        }
        return null;
    }
}
