package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;


public class RemindTargetDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        DestinationReminderManager destinationReminder = DestinationReminderManager.getInstance();
        String instructions = """
                use the data provided to remind the user what where we going and what we are doing
                """;
        return process(new AiDataStruct(instructions, new DataDto(destinationReminder.getReminderAsJson())), originalUserInput);
    }

    record DataDto(String data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
