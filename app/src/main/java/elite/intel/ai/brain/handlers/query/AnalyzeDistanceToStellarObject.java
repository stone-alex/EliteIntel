package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeDistanceToStellarObject extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing local star system data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        return process(
                new AiDataStruct(
                        "Return 'distance' in light seconds to the stellar object or station requested by the user. User may give stellarObjectName as stellar object identification. or Phonetic name of a station",
                        new DataDto(playerSession.getLocations())
                ),
                originalUserInput
        );
    }

    record DataDto(Map<Long, LocationDto> locations) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
