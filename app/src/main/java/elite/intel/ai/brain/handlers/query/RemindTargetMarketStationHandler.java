package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.search.spansh.station.DestinationDto;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class RemindTargetMarketStationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        DestinationReminderManager reminder = DestinationReminderManager.getInstance();
        DestinationDto destination = reminder.getDestination();

        if (destination == null) {
            return process("No target market station is set.");
        }

        return process(new AiDataStruct("Remind the user stationName where market is located", new DataDto(destination)), originalUserInput);
    }

    private record DataDto(DestinationDto destination) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
