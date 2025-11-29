package elite.intel.search.spansh.traderoute;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class TradeRouteSearchCriteria implements ToJsonConvertible {

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
