package elite.intel.ai.search.spansh.station.traderandbroker;

import com.google.gson.JsonObject;
import elite.intel.ai.search.spansh.station.StationSearch;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SearchForMaterialBrokerOrTrader {

    private static final Logger log = LogManager.getLogger(SearchForMaterialBrokerOrTrader.class);

    public static List<TraderAndBrokerSearchDto.Result> findMaterialTrader(ToJsonConvertible searchCriteria) {

        try {
            JsonObject getJson = StationSearch.getInstance().performSearch(searchCriteria).getAsJsonObject();
            TraderAndBrokerSearchDto dto = GsonFactory.getGson().fromJson(getJson, TraderAndBrokerSearchDto.class);
            return dto.getResults();
        } catch (Exception e) {
            log.error("Failed to find material trader", e);
        }
        return null;
    }
}
