package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.DistanceCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AnalyzeDistanceToFinalDestination extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto here = playerSession.getCurrentLocation();
        Map<String, NavRouteDto> route = playerSession.getRoute();

        if (here == null) {
            return analyzeData(toJson("Current location data unavailable."), originalUserInput);
        }
        if (route == null || route.isEmpty()) {
            return analyzeData(toJson("No route data available."), originalUserInput);
        }

        // Create NavRouteDto for current location
        NavRouteDto currentLocation = new NavRouteDto();
        currentLocation.setLeg(-1); // Distinct from route legs
        currentLocation.setName(here.getStarName());
        currentLocation.setX(here.getX());
        currentLocation.setY(here.getY());
        currentLocation.setZ(here.getZ());

        // Sort route by leg
        List<NavRouteDto> orderedRoute = new ArrayList<>(route.values());
        orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));

        String distanceData = getDistanceDataForAnnouncement(currentLocation, orderedRoute);
        // AI cadence or error response
        return analyzeData(toJson(distanceData), originalUserInput);
    }

    private static String getDistanceDataForAnnouncement(NavRouteDto currentSystem, List<NavRouteDto> route) {
        if (route.isEmpty()) {
            return "No route data available.";
        }

        double totalDistance = 0.0;
        NavRouteDto previous = currentSystem;

        // Sum distances for all legs, starting from current system
        for (NavRouteDto next : route) {
            double legDistance = DistanceCalculator.calculateDistance(
                    previous.getX(), previous.getY(), previous.getZ(),
                    next.getX(), next.getY(), next.getZ()
            );
            totalDistance += legDistance;
            previous = next;
        }

        // Straight-line distance from current to final system
        NavRouteDto last = route.get(route.size() - 1);
        double straightLineDistance = DistanceCalculator.calculateDistance(
                currentSystem.getX(), currentSystem.getY(), currentSystem.getZ(),
                last.getX(), last.getY(), last.getZ()
        );

        StringBuilder sb = new StringBuilder();
        sb.append("The straight-line distance is ");
        sb.append(String.format("%.2f", straightLineDistance));
        sb.append(" light years, ");
        sb.append(" The total route distance from ");
        sb.append(currentSystem.getName());
        sb.append(" to ");
        sb.append(last.getName());
        sb.append(" is ");
        sb.append(String.format("%.2f", totalDistance));
        sb.append(" light years, accounting for "+route.size()+" actual jumps.");

        return sb.toString();
    }
}