package elite.intel.ai.search.spansh.findcarrier;

import elite.intel.ai.search.spansh.client.SpanshClient;

public class FindFleetCarrierClient extends SpanshClient {

    private static FindFleetCarrierClient instance;

    private FindFleetCarrierClient() {
        super("https://spansh.co.uk/api/stations/search/save", "https://spansh.co.uk/api/stations/search/recall/");
    }

    public static synchronized FindFleetCarrierClient getInstance() {
        if (instance == null) {
            instance = new FindFleetCarrierClient();
        }
        return instance;
    }
}
