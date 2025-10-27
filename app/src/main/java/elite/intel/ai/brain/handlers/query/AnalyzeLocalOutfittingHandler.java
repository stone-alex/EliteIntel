package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.search.edsm.dto.OutfittingDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_LOCAL_OUTFITTING;

public class AnalyzeLocalOutfittingHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        OutfittingDto outfitting = playerSession.getCurrentLocation().getOutfitting();

        return process(new AiDataStruct(ANALYZE_LOCAL_OUTFITTING.getInstructions(), new DataDto(outfitting)), originalUserInput);
    }

    private record DataDto(OutfittingDto outfitting) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
