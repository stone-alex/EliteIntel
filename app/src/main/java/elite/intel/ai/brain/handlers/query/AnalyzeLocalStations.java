package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.OutfittingDto;
import elite.intel.search.edsm.dto.ShipyardDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_LOCAL_STATIONS;

public class AnalyzeLocalStations extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing stations data... stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        StationsDto stationsDto = EdsmApiClient.searchStations(playerSession.getCurrentLocation().getStarName());
        List<DataElement> data = new ArrayList<>();
        if(stationsDto.getData() != null && stationsDto.getData().getStations() != null) {
            stationsDto.getData().getStations().forEach(station -> {
                OutfittingDto outfitting = EdsmApiClient.searchOutfitting(station.getMarketId(), null, null);
                ShipyardDto shipyard = EdsmApiClient.searchShipyard(station.getMarketId(), null, null);
                data.add(new DataElement(station.getName(), outfitting, shipyard, currentLocation));
            });
        }

        return process(new AiDataStruct(ANALYZE_LOCAL_STATIONS.getInstructions(), new DataDto(data)),originalUserInput);
    }

    record DataElement(String stationName, OutfittingDto outfitting, ShipyardDto shipyard, LocationDto currentLocation) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto(List<DataElement> data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
