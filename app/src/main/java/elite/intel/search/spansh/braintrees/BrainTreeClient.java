package elite.intel.search.spansh.braintrees;

import elite.intel.search.spansh.client.SpanshClient;

public class BrainTreeClient extends SpanshClient {

    private static final String BASE_URL = "https://spansh.co.uk/api/bodies/search/save";
    private static final String RESULTS_URL = "https://spansh.co.uk/api/bodies/search/recall/";

    private static volatile BrainTreeClient instance;

    private BrainTreeClient() {
        super(BASE_URL, RESULTS_URL);
    }
    
    public static BrainTreeClient getInstance() {
        if (instance == null) {
            synchronized (BrainTreeClient.class) {
                if (instance == null) {
                    instance = new BrainTreeClient();
                }
            }
        }
        return instance;
    }
}
