package elite.intel.db;

import elite.intel.db.managers.TradeRouteManager;

public class MainTest {
    public static void main(String[] args) {
        TradeRouteManager manager = TradeRouteManager.getInstance();
        manager.calculateTradeRoute();
    }
}
