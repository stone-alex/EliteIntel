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

    private StarSystemClient() {
        super("https://spansh.co.uk/api/systems/search/save", "https://spansh.co.uk/api/systems/search/recall/");
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

    public List<StationSearchResult.SystemResult> searchStarSystems(SystemSearchCriteria criteria) {
        StationSearchResult stationSearchResult = GsonFactory.getGson().fromJson(performSearch(criteria), StationSearchResult.class);
        if (stationSearchResult == null) return null;
        List<StationSearchResult.SystemResult> results = stationSearchResult.getResults();
        return results;
    }
}
