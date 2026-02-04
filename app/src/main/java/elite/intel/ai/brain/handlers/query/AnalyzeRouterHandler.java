package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static elite.intel.util.NavigationUtils.calculateGalacticDistance;

public class AnalyzeRouterHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Logger log = LogManager.getLogger(AnalyzeRouterHandler.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing route telemetry... Stand By..."));
        Collection<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        LocationDto here = locationManager.findByLocationData(playerSession.getLocationData());

        // Create NavRouteDto for current location
        NavRouteDto currentLocation = new NavRouteDto();
        currentLocation.setLeg(-1); // Distinct from route legs
        currentLocation.setName(here.getStarName());
        currentLocation.setX(here.getX());
        currentLocation.setY(here.getY());
        currentLocation.setZ(here.getZ());

        String distanceData = getDistanceDataForAnnouncement(currentLocation, orderedRoute);
        
        String instructions = """
                Analyze the current plotted ship-route using ONLY the provided JSON data under "content". NEVER invent, assume, calculate, add external information, or guess — use ONLY the exact fields present.
                
                Output MUST be TTS-friendly: natural spoken English, short clear sentences, NO markdown tables, NO bold/italics/code, NO long lists unless specifically requested. Speak numbers naturally (twelve, eight hundred thirty-seven point three nine). Keep responses very short and focused — never more than 100–150 words unless the user explicitly asks for the full route.
                
                ## Critical Rules — Do NOT violate these:
                - This action does NOT automatically return the complete route breakdown.
                - Answer ONLY the specific question the user asked.
                - Do NOT give unsolicited full-route summaries, leg lists, or totals unless the user requests:
                  • "full route", "all waypoints", "complete route", "entire plotted route", "list all legs", "show the whole thing"
                - If the user asks about one thing (next jump, next star, scoopable status, remaining jumps, a specific leg, etc.), answer ONLY that — be extremely concise.
                
                ## Quick Reference — How to Answer Common Requests:
                - "next waypoint / next jump / next stop / next star": \s
                  ONLY leg 1 (data[0]): "Next waypoint is [starName], [starClass] class. Scoopable: yes/no. Jumps remaining after: [remainingJumps]."
                
                - "how many jumps left / remaining jumps / jumps remaining": \s
                  ONLY: "You have [jumpsRemaining] jumps remaining to the final destination."
                
                - "distance / how far": \s
                  ONLY: "[distanceToFinal spoken verbatim]"
                
                - "is it scoopable / fuel scoopable": \s
                  If asking about next → use data[0].isScoopable \s
                  If asking about whole route → "All [data.length] waypoints are scoopable." (or note exceptions if any false)
                
                - "what class is the next star": \s
                  "Next star is [starClass] class."
                
                - "traffic / casualties / security / hazards": \s
                  "No traffic, casualty, or security information is available in the plotted route data."
                
                - If user says "full route" or similar: THEN give totals first + simple spoken list of legs (short format: "Leg 1: [name], [class]. Leg 2: …" — group if long)
                
                ALWAYS start directly with the answer — no preamble like "According to the data…". \s
                End without repeating totals unless asked.
                """;
        return process(new AiDataStruct(instructions, new DataDto(toStopsData(orderedRoute), orderedRoute.size(), distanceData)), originalUserInput);
    }

    private List<StopData> toStopsData(Collection<NavRouteDto> orderedRoute) {
        LinkedList<StopData> result = new LinkedList<>();
        for(NavRouteDto leg : orderedRoute){
            result.add(new StopData(leg.getLeg(), leg.getRemainingJumps(), leg.getStarClass(), leg.getName(), leg.isScoopable()));
        }
        return result;
    }


    private static String getDistanceDataForAnnouncement(NavRouteDto currentSystem, Collection<NavRouteDto> route) {
        if (route.isEmpty()) {
            return "No route data available.";
        }

        double totalDistance = 0.0;
        NavRouteDto previous = currentSystem;

        // Sum distances for all legs, starting from current system
        for (NavRouteDto next : route) {
            double legDistance = calculateGalacticDistance(
                    previous.getX(), previous.getY(), previous.getZ(),
                    next.getX(), next.getY(), next.getZ()
            );
            totalDistance += legDistance;
            previous = next;
        }

        // Straight-line distance from current to a final system
        NavRouteDto last = route.stream().reduce((first, second) -> second).orElse(null);
        double straightLineDistance = calculateGalacticDistance(
                currentSystem.getX(), currentSystem.getY(), currentSystem.getZ(),
                last.getX(), last.getY(), last.getZ()
        );

        StringBuilder sb = new StringBuilder();
        sb.append("The straight-line distance is ");
        sb.append(String.format("%.2f", straightLineDistance));
        sb.append(" light years, ");
        sb.append(" The total route distance");
        sb.append(" is ");
        sb.append(String.format("%.2f", totalDistance));
        sb.append(" light years, accounting for "+route.size()+" actual jumps.");

        return sb.toString();
    }

    record StopData(int legNumber, int remainingJumps, String starClass, String starName, boolean isScoopable) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
    
    record DataDto(List<StopData> data, int jumpsRemaining, String distanceToFinal) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
