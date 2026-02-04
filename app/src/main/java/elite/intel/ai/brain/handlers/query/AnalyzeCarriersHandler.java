package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.search.edsm.dto.data.Station;
import elite.intel.search.edsm.dto.data.StationsData;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeCarriersHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String primaryStarName = playerSession.getPrimaryStarName();
        StationsDto stationsDto = EdsmApiClient.searchStations(primaryStarName, 0);
        ArrayList<StationData> stationsData = new ArrayList<>();
        StationsData data = stationsDto.getData();
        for (Station station : data.getStations()) {
            /// skip all fleet carriers. (query carriers in another handler to save on tokens)
            if ("Fleet Carrier".equalsIgnoreCase(station.getType())) {
                stationsData.add(new StationData(
                        station.getName(),
                        station.getCommodities() == null ? null : station.getCommodities().size(),
                        station.getOtherServices() == null ? null : station.getOtherServices().size()
                ));
            }
        }


        String instructions = """
                    Summarize the fleet carriers in the star system by type. How many present, How many carriers have commodities? How many carriers have services?
                    Example X carriers present, Y have commodities (IF no commodity - none have commodity), Z have services (IF non have services - non have services).
                """;

        return process(new AiDataStruct(instructions, new DataDto(stationsData)), originalUserInput);
    }

    record StationData(String name, Integer numberOfCommodities, Integer numberOfServices) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }


    record DataDto(List<StationData> carriers) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

