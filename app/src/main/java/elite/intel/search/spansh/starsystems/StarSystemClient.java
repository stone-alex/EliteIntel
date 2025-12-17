package elite.intel.search.spansh.starsystems;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.search.spansh.client.SpanshClient;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class StarSystemClient extends SpanshClient {

    private static volatile StarSystemClient instance;
    private final Logger log = LogManager.getLogger(StarSystemClient.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = GsonFactory.getGson();

    private StarSystemClient() {
        super("https://spansh.co.uk/api/systems/search/save", "https://spansh.co.uk/api/stations/search/recall/");
    }

    public static StarSystemClient getInstance() {
        if (instance == null) {
            synchronized (StarSystemClient.class) {
                if (instance == null) {
                    instance = new StarSystemClient();
                }
            }
        }
        return instance;
    }

    public StarSystemResult search(SystemSearchCriteria criteria) {
        // search will return stations.
        // we have to make a GET call to https://spansh.co.uk/api/system/2209500629347 where the number is
        // system_id64
        StationSearchResult stationSearchResult = GsonFactory.getGson().fromJson(performSearch(criteria), StationSearchResult.class);
        if (stationSearchResult == null) return null;
        List<StationSearchResult.SystemResult> results = stationSearchResult.getResults();
        for (StationSearchResult.SystemResult result : results) {
            if (result.getId64() == 0) continue;
            HttpRequest post = HttpRequest.newBuilder()
                    .uri(URI.create("https://spansh.co.uk/api/system/" + result.getId64()))
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .GET()
                    .build();


            try {
                HttpResponse<String> resp = httpClient.send(post, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 400) {
                    log.warn("POST failed: {}", resp.body());
                    EventBusManager.publish(new SensorDataEvent("Unable to complete Shapnsh request: " + resp.body()));
                }

                if (resp.statusCode() == 200) {
                    JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
                    StarSystemResult starSystem = GsonFactory.getGson().fromJson(json, StarSystemResult.class);
                    if (starSystem.getRecord().getName().equalsIgnoreCase(criteria.getFilters().getSystemName().getValue())) {
                        return starSystem;
                    }
                }
            } catch (IOException | InterruptedException e) {
                // keep going
            }
        }
        return null;
    }

}
