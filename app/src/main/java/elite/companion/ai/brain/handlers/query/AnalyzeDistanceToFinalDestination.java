package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.DistanceCalculator;
import elite.companion.util.json.GsonFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AnalyzeDistanceToFinalDestination extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Map<String, NavRouteDto> route = playerSession.getRoute();

        // It is a LinkedHashMap, but it is serialized and deserialized via Gson which defaults to -
        // LinkedTreeMap, which is key-sorted. We can't guarantee the order of the route,
        // so we sort it by 'leg'
        List<NavRouteDto> orderedRoute = new ArrayList<>(route.values());
        orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));

        String distanceData = getDistanceDataForAnnouncement(orderedRoute);

        // Direct vocalization
        // EventBusManager.publish(new VoiceProcessEvent(distanceData));

        // AI cadence or error response
        return analyzeData(GSON.toJson(distanceData), originalUserInput);
    }

    private static String getDistanceDataForAnnouncement(List<NavRouteDto> route) {
        if (route.size() < 2) {
            return "Insufficient route data to calculate distance.";
        }

        double totalDistance = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            NavRouteDto current = route.get(i);
            NavRouteDto next = route.get(i + 1);
            totalDistance += DistanceCalculator.calculateDistance(
                    current.getX(), current.getY(), current.getZ(),
                    next.getX(), next.getY(), next.getZ()
            );
        }

        NavRouteDto first = route.get(0);
        NavRouteDto last = route.get(route.size() - 1);
        StringBuilder sb = new StringBuilder();
        sb.append("The total route distance from ");
        sb.append(first.getName());
        sb.append(" to ");
        sb.append(last.getName());
        sb.append(" is ");
        sb.append(String.format("%.2f", totalDistance));
        sb.append(" light years.");

        return sb.toString();
    }
}