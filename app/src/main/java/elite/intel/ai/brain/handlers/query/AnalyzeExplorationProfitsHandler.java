package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.CodexEntryDao;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.Collection;
import java.util.List;

public class AnalyzeExplorationProfitsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing exploration data... Stand by..."));

        String instructions = """
                Use this data to provide answers on potential exo-biology exploration profits in credits.
                potentialProfit is a sum of credits user can receive this session (could have)
                acquiredProfit is a sum of all credits user actually acquired this session. (will have)
                """;
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(calculatePotentialProfit(), calculateActualProfit())),
                originalUserInput
        );
    }

    private long calculateActualProfit() {
        List<BioSampleDto> allCompletedBioSamples = playerSession.getBioCompletedSamples();
        long result = 0;
        for (BioSampleDto dto : allCompletedBioSamples) {
            result = result + dto.getPayout();
        }
        List<CodexEntryDao.CodexEntry> allCodexEntries = codexEntryManager.findAll();
        for (CodexEntryDao.CodexEntry entry : allCodexEntries) {
            result = result + entry.getVoucherAmount();
        }
        return result;
    }

    private long calculatePotentialProfit() {
        Collection<LocationDto> stellarObjects = locationManager.findAllBySystemAddress(playerSession.getLocationData().getSystemAddress());
        long result = 0;
        for (LocationDto dto : stellarObjects) {
            List<GenusDto> genus = dto.getGenus();
            for (GenusDto g : genus) {
                result = result + g.getRewardInCredits() + g.getBonusCreditsForFirstDiscovery();
            }
        }
        return result;
    }

    record DataDto(long potentialProfit, long acquiredProfit) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
