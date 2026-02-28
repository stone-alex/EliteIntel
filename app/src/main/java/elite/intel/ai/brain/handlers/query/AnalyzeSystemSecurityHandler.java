package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeSystemSecurityHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing system security. Stand by."));

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        DeathsDto deathsDto = currentLocation.getDeathsDto();
        TrafficDto trafficDto = currentLocation.getTrafficDto();

        String instructions = """
                Provided security and safety assessment based on this data.
                Power play state is indicated by powerPlayState and powerPlayStateControlProgress
                However even if system does not have major power play it may have controlling faction and local government.
                """;
        double pscp = currentLocation.getPowerplayStateControlProgress();
        String powerPlayStateControlProgress = pscp == 0 ? "none" : String.valueOf(pscp);
        return process(
                new AiDataStruct(instructions,
                        new DataDto(
                                currentLocation.getStarName(),
                                trafficDto == null ? null : trafficDto.getData().getDiscovery(),
                                currentLocation.getSecurity(),
                                currentLocation.getStationFaction(),
                                currentLocation.getPopulation(),
                                currentLocation.getSecondEconomy(),
                                currentLocation.getGovernment(),
                                currentLocation.getPowerplayState(),
                                currentLocation.getControllingPower(),
                                powerPlayStateControlProgress,
                                currentLocation.getPowerplayStateReinforcement(),
                                currentLocation.getPowerplayStateUndermining(),
                                deathsDto == null ? null : deathsDto.getData().getDeaths(),
                                trafficDto == null ? null : trafficDto.getData().getTraffic()
                        )
                ),
                originalUserInput
        );
    }

    record DataDto(
            String starSystem,
            ToYamlConvertable discoveredBy,
            String security,
            String faction,
            Long population,
            String economy,
            String government,
            String powerPlayState,
            String controllingPower,
            String powerplayStateControlProgress,
            Integer powerplayStateReinforcement,
            Integer powerplayStateUndermining,
            ToYamlConvertable deathsDto,
            ToYamlConvertable trafficDto
    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
