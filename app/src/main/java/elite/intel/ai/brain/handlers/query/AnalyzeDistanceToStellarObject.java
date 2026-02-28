package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.Collection;

public class AnalyzeDistanceToStellarObject extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing local star system data. Stand by."));

        String instructions = """
                Return 'distance' in light seconds to the stellar object or station requested by the user.
                User may give stellarObjectName as stellar object identification, or Phonetic name of a station
                """;

        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                locationManager.findAllBySystemAddress(
                                        playerSession.getLocationData().getSystemAddress()
                                )
                        )
                ),
                originalUserInput
        );
    }

    record DataDto(Collection<LocationDto> locations) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
