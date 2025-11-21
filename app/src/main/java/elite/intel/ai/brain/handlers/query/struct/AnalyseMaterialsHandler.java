package elite.intel.ai.brain.handlers.query.struct;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.util.Database;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_MATERIALS_ON_HAND;

public class AnalyseMaterialsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Accessing materials data. Stand by."));
        List<MaterialsDao.Material> materials = Database.withDao(MaterialsDao.class, dao -> dao.listAll());
        return process(
                new AiDataStruct(
                        ANALYZE_MATERIALS_ON_HAND.getInstructions(),
                        new DataDto(materials)
                ),
                originalUserInput
        );
    }

    record DataDto(List<MaterialsDao.Material> materials) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
