package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.dao.RouteMonetisationDao;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;


public class RemindTargetDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final DestinationReminderManager destinationReminder = DestinationReminderManager.getInstance();
    private final MonetizeRouteManager monetizeRouteManager = MonetizeRouteManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        String reminder = destinationReminder.getReminderAsJson();
        if(reminder != null){
            return process(reminder);
        }

        RouteMonetisationDao.MonetisationTransaction monetizeRouteReminder = monetizeRouteManager.getTransaction();
        if (monetizeRouteReminder != null) {
            // monetized hop.
            return process(
                    new AiDataStruct(
                            "Remind user what commodity we are buying, at what port and what star system.",
                            new MonetizeRouteReminder(
                                    monetizeRouteReminder.getSourceStationName(),
                                    monetizeRouteReminder.getSourceCommodity(),
                                    monetizeRouteReminder.getDestinationStarSystem())
                    ),
                    originalUserInput
            );

        }  else {
            return process("No reminder set.");
        }
    }

    record MonetizeRouteReminder(String pickupAtStation, String commodity, String dropOffLocation) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}