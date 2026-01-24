package elite.intel.ai.brain.handlers.query.struct;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import javax.swing.plaf.InsetsUIResource;
import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;

public class AnalyseMaterialsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Accessing materials data. Stand by."));
        String mat = params.get("key").getAsString();
        if(mat == null) return process("Material parameter not provided. Unable to run analysis");

        String material = capitalizeWords(
                        FuzzySearch.fuzzyMaterialSearch(
                                mat, 3
                        )
                );

        MaterialsDao.Material data = Database.withDao(MaterialsDao.class, dao -> dao.findByExactName(material));

        String instructions = """
                Provide answers about material on hand based on this data.
                Material amount is measured in units. 
                Example Answer:  {"type":"chat", "response_text":"We have 12 units of mercury out of 200"}
                """;

        return process(new AiDataStruct(instructions, new DataDto(data)), originalUserInput);
    }

    record DataDto(MaterialsDao.Material materials) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
