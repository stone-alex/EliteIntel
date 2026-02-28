package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.search.edsm.dto.data.Station;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.LinkedList;
import java.util.List;

public class AnalyzeLocalStations extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing stations data. Stand by."));

        String starName = playerSession.getPrimaryStarName();
        StationsDto stationsDto = EdsmApiClient.searchStations(starName, 0);
        List<Station> stations = stationsDto.getData().getStations();
        if (stations == null || stations.isEmpty()) {
            return process("no data available");
        }

        List<StationData> stationData = new LinkedList<>();
        stations.forEach(s -> stationData.add(
                new StationData(
                        s.getType(),
                        s.getName(),
                        s.getBody() == null ? null : s.getBody().getName(),
                        s.getDistanceToArrivalInLightSeconds(),
                        s.getAllegiance(),
                        s.getGovernment(),
                        s.getEconomy(),
                        s.isHaveMarket(),
                        s.isHaveShipyard(),
                        s.isHaveOutfitting(),
                        s.getControllingFaction() == null ? null : s.getControllingFaction().getName()
                )
        ));

        String instructions = """
                Answer questions about local stations.
                """;

        return process(
                new AiDataStruct(instructions,
                        new DataDto(starName, stationData)
                ), originalUserInput
        );
    }

    record DataDto(String starSystemName, List<StationData> stations) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record StationData(
            String stationType,
            String stationName,
            String orbitingAround,
            double distanceToArrivalInLightSeconds,
            String allegiance,
            String government,
            String economy,
            boolean haveMarket,
            boolean haveShipyard,
            boolean haveOutfitting,
            String controllingFaction

    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
