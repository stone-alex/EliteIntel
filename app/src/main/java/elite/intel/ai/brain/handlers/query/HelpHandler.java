package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.dao.HelpDao;
import elite.intel.db.util.Database;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) {

        JsonElement key = params.get("key");
        String topic = key == null ? null : key.getAsString();
        if (topic == null) {
            return process("No help topic provided");
        }

        Map<String, String> data = Database.withDao(HelpDao.class, dao -> {
            Map<String, String> map = new HashMap<>();
            String[] queries = topic.split(" ");
            for (String q : queries) {
                List<HelpDao.HelpEntity> help = dao.getHelp(q, q);
                for (HelpDao.HelpEntity h : help) {
                    map.put(h.getTopic(), h.getHelpText());
                }
            }
            return map;
        });

        return process(new AiDataStruct("Use this data to explain to the user how you can assist with a given task. Provide response in text format.", new DataDto(data)), originalUserInput);

    }

    record DataDto(Map<String, String> data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}