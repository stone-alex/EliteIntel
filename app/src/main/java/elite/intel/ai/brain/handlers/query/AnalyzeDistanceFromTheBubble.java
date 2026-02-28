package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.NavigationUtils;

public class AnalyzeDistanceFromTheBubble extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing galactic coordinates. Stand by."));
        LocationDao.Coordinates galacticCoordinates = LocationManager.getInstance().getGalacticCoordinates();

        if (galacticCoordinates.x() == 0 && galacticCoordinates.y() == 0 && galacticCoordinates.z() == 0) {
            return process("Local Coordinates are not available.");
        }

        double distance = NavigationUtils.calculateGalacticDistance(
                0.0, 0.0, 0.0,
                galacticCoordinates.x(),
                galacticCoordinates.y(),
                galacticCoordinates.z()
        );
        int distLy = (int) Math.round(distance);
        double jumps = distLy / 500.0;
        int fuelTons = (int) Math.round(jumps * 100);
        double totalMinutes = jumps * 20;
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);

        String responseText = String.format(
                "%d light years, carrier fuel required %d tons, travel time %d hours and %d minutes",
                distLy, fuelTons, hours, minutes
        );

        return process(responseText);
    }
}
