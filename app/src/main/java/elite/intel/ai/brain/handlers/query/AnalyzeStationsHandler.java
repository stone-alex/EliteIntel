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

public class AnalyzeStationsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String primaryStarName = playerSession.getPrimaryStarName();
        StationsDto stationsDto = EdsmApiClient.searchStations(primaryStarName, 0);
        ArrayList<StationData> stationsData = new ArrayList<>();
        StationsData data = stationsDto.getData();
        if (data != null && !data.getStations().isEmpty()) {
            for (Station station : data.getStations()) {
                /// skip all fleet carriers. (query carriers in another handler to save on tokens)
                if ("Fleet Carrier".equalsIgnoreCase(station.getType())) continue;
                stationsData.add(new StationData(
                        station.getName(),
                        station.getType(),
                        station.getAllegiance(),
                        station.getGovernment(),
                        station.getCommodities() == null ? null : station.getCommodities().size(),
                        station.getEconomy(),
                        station.getOtherServices() == null ? null : station.getOtherServices().size()
                ));
            }
        }


        String instructions = """
                    Summarize the stations in the star system by type. Provide information about government and allegiance.
                """;

        return process(new AiDataStruct(instructions, new DataDto(stationsData)), originalUserInput);
    }

    record StationData(String name, String type, String allegiance, String government, Integer numberOfCommodities, String economy, Integer numberOfServices) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }


    record DataDto(List<StationData> stations) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
