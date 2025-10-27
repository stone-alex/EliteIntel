package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collection;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_NEXT_STAR_SCOOPABLE;

public class AnalyzeNextStarForFuelHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Collection<? extends ToJsonConvertible> route = playerSession.getOrderedRoute();

        String data = JsonDataFactory.getInstance().toJsonArrayString(route);
        return process(new AiDataStruct(QUERY_NEXT_STAR_SCOOPABLE.getInstructions(), new DataDto(data)), originalUserInput);
    }

    record DataDto(String data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
