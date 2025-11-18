package elite.intel.ai.search.spansh.station;

import elite.intel.ai.search.spansh.client.SpanshClient;

public class StationSearchClient extends SpanshClient {

    private static StationSearchClient instance;

    private StationSearchClient() {
        super("https://spansh.co.uk/api/stations/search/save", "https://spansh.co.uk/api/stations/search/recall/");
    }

    public static synchronized StationSearchClient getInstance() {
        if (instance == null) {
            instance = new StationSearchClient();
        }
        return instance;
    }
}
