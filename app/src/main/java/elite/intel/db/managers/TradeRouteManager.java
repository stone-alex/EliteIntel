package elite.intel.db.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeRouteManager {
    private static TradeRouteManager instance;
    private final Logger log = LogManager.getLogger(TradeRouteManager.class);

    private TradeRouteManager() {
    }

    public static synchronized TradeRouteManager getInstance() {
        if (instance == null) {
            instance = new TradeRouteManager();
        }
        return instance;
    }
}
