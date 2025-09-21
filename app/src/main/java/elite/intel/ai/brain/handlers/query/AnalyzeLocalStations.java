package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.OutfittingDto;
import elite.intel.ai.search.edsm.dto.ShipyardDto;
import elite.intel.ai.search.edsm.dto.StationsDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeLocalStations extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        String starName = playerSession.getCurrentLocation() == null ? "no data" : playerSession.getCurrentLocation().getStarName();
        StationsDto stationsDto = EdsmApiClient.searchStations(starName);
        List<DataDto> data = new ArrayList<>();
        stationsDto.getData().getStations().forEach(station -> {
            OutfittingDto outfitting = EdsmApiClient.searchOutfitting(station.getMarketId(), null, null);
            ShipyardDto shipyard = EdsmApiClient.searchShipyard(station.getMarketId(), null, null);
            data.add(new DataDto(station.getName(), outfitting, shipyard));
        });

        return analyzeData(toJson(data), originalUserInput);
    }

    record DataDto(String stationName, OutfittingDto outfitting, ShipyardDto shipyard) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
