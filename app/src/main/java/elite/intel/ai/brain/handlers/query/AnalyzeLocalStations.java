package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.OutfittingDto;
import elite.intel.search.edsm.dto.ShipyardDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeLocalStations extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing stations data... Stand by..."));

        StationsDto stationsDto = EdsmApiClient.searchStations(playerSession.getPrimaryStarName(), 0);
        List<DataElement> data = new ArrayList<>();
        if (stationsDto.getData() != null && stationsDto.getData().getStations() != null) {
            stationsDto.getData().getStations().forEach(station -> {
                OutfittingDto outfitting = EdsmApiClient.searchOutfitting(station.getMarketId(), null, null);
                ShipyardDto shipyard = EdsmApiClient.searchShipyard(station.getMarketId(), null, null);
                data.add(new DataElement(station.getName(), outfitting, shipyard));
            });
        }

        return process(new AiDataStruct("Answer questions about local stations", new DataDto(data)), originalUserInput);
    }

    record DataElement(String stationName, OutfittingDto outfitting, ShipyardDto shipyard) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto(List<DataElement> data) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
