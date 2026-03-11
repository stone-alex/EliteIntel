package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.List;

public class AnalyzeLastScanHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing scanner. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto lastScan = playerSession.getLastScan();

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();


        String instructions = """
                Summarize scanner data. Or use this data to answer specific questions.
                """;
        return process(new AiDataStruct(instructions, new DataDto(lastScan, partialScans)), originalUserInput);
    }

    record DataDto(LocationDto lastScan, List<BioSampleDto> partialScans) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
