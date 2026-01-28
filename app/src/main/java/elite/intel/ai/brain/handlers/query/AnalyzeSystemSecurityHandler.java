package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeSystemSecurityHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing system security... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        DeathsDto deathsDto = currentLocation.getDeathsDto();
        TrafficDto trafficDto = currentLocation.getTrafficDto();

        String instructions = "Provided security and safety assessment based on this data";
        return process(
                new AiDataStruct(instructions,
                        new DataDto(
                                currentLocation.getSecurity(),
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
