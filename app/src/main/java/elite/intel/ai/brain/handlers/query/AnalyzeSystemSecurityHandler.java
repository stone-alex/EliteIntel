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
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeSystemSecurityHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing system security... Stand by..."));

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        DeathsDto deathsDto = currentLocation.getDeathsDto();
        TrafficDto trafficDto = currentLocation.getTrafficDto();

        String instructions = """
                Provided security and safety assessment based on this data.
                Power play state is indicated by powerplayState and powerplayStateControlProgress
                However even if system does not have major power play it may have controlling faction and local government.
                """;
        return process(
                new AiDataStruct(instructions,
                        new DataDto(
                                currentLocation.getSecurity(),
                                currentLocation.getStationFaction(),
                                currentLocation.getPopulation(),
                                currentLocation.getSecondEconomy(),
                                currentLocation.getGovernment(),
                                currentLocation.getPowerplayState(),
                                currentLocation.getControllingPower(),
                                currentLocation.getPowerplayStateControlProgress(),
                                currentLocation.getPowerplayStateReinforcement(),
                                currentLocation.getPowerplayStateUndermining(),
                                deathsDto,
                                trafficDto
                        )
                ),
                originalUserInput
        );
    }

    record DataDto(
                String security,
                String faction,
                Long population,
                String economy,
                String government,
                String powerplayState,
                String controllingPower,
                double powerplayStateControlProgress,
                Integer powerplayStateReinforcement,
                Integer powerplayStateUndermining,
                DeathsDto deathsDto,
                TrafficDto trafficDto
        ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
