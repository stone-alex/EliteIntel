package elite.intel.ai.brain.handlers.query.struct;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import static elite.intel.util.StringUtls.capitalizeWords;

public class AnalyseMaterialsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Accessing materials data. Stand by."));
        JsonElement key = params.get("key");
        if (key == null) return process("Material parameter not provided. Unable to run analysis");
        String mat = key.getAsString();
        if(mat == null) return process("Material parameter not provided. Unable to run analysis");

        String material = capitalizeWords(
                        FuzzySearch.fuzzyInventorySearch(
                                mat, 8
                        )
                );

        MaterialsDao.Material data = Database.withDao(MaterialsDao.class, dao -> dao.findByExactName(material));
        if (data == null) {
            return process("No data found for material: " + material);
        }

        String instructions = """
                Answer the user's question about this material in the ship's inventory.
                
                Data fields:
                - materialName: name of the material
                - materialType: category of the material
                - amount: current units held
                - maxCap: maximum storage capacity in units
                
                State the amount held and maximum capacity. Answer only what was asked.
                """;

        return process(new AiDataStruct(instructions, new DataDto(data)), originalUserInput);
    }

    record DataDto(MaterialsDao.Material materials) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
