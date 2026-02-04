package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeLocalMarketsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing local market data... Stand by..."));
        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        MarketDto market = currentLocation.getMarket();
        return process(new AiDataStruct("Use markets data to provide answers.", new DataDto(market)), originalUserInput);
    }

    private record DataDto(MarketDto market) implements ToYamlConvertable{
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
