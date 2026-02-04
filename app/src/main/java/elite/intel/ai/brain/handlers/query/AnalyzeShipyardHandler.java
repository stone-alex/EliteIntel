package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.ShipyardDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;


public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing shipyard data... Stand by..."));

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        ShipyardDto shipyard = currentLocation.getShipyard();

        String instructions = """
            Answer questions about shipyard contents.
            If no data available return no data available
        """;

        return process(new AiDataStruct(instructions, new DataDto(shipyard)), originalUserInput);
    }

    private record DataDto(ToYamlConvertable shipyard) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
