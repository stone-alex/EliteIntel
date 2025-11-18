package elite.intel.ai.search.spansh.stellarobjects;

import elite.intel.ai.search.spansh.client.SpanshClient;

public class StellarObjectSearchClient extends SpanshClient {

    private static StellarObjectSearchClient instance;

    private StellarObjectSearchClient() {
        super("https://spansh.co.uk/api/bodies/search/save", "https://spansh.co.uk/api/bodies/search/recall/");
    }

    public static synchronized StellarObjectSearchClient getInstance() {
        if (instance == null) {
            instance = new StellarObjectSearchClient();
        }
        return instance;
    }
}
