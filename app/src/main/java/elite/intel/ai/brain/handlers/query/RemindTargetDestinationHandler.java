package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.session.DestinationReminder;

import static elite.intel.ai.brain.handlers.query.Queries.TARGET_STATION_REMINDER;

public class RemindTargetDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        DestinationReminder destinationReminder = DestinationReminder.getInstance();
        return process(new AiDataStruct(TARGET_STATION_REMINDER.getInstructions(), destinationReminder.getDestination()), originalUserInput);
    }
}
