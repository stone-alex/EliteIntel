package elite.intel.search.spansh.carrierroute;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.search.spansh.client.SpanshClient;
import elite.intel.search.spansh.client.StringQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * Client for querying spansh.co.uk fleet carrier routing API.
 * Handles POST to queue a route job and immediate GET for results, with custom fuel calculation.
 */
public class SpanshCarrierRouteClient extends SpanshClient {

    private static final Logger log = LogManager.getLogger(SpanshCarrierRouteClient.class);
    private static final String BASE_URL = "https://spansh.co.uk/api/fleetcarrier/route";
    private static final String RESULTS_URL = "https://spansh.co.uk/api/results/";

    public SpanshCarrierRouteClient() {
        super(BASE_URL, RESULTS_URL);
    }

    /**
     * Calculates a fleet carrier route based on criteria and current fuel supply.
     *
     * @param criteria   Search criteria for the route.
     * @param fuelSupply Current fuel level in tons.
     * @return Map of jump index (1-based) to CarrierJump details, or empty map on failure.
     */
    public Map<Integer, CarrierJump> calculateRoute(CarrierRouteCriteria criteria, int fuelSupply) {
        // Build form-urlencoded payload
        String payload = "source=" + URLEncoder.encode(criteria.sourceSystem(), StandardCharsets.UTF_8) +
                "&destinations=" + URLEncoder.encode(criteria.destinationSystem(), StandardCharsets.UTF_8) +
                "&capacity=" + criteria.capacity() +
                "&capacity_used=" + criteria.capacityUsed() +
                "&calculate_starting_fuel=" + criteria.calculateStartingFuel();


        /// search
        JsonObject result = performSearch(new Request(payload));
        if (result == null || result.get("result") == null || result.get("result").getAsJsonObject().get("jumps") == null) {
            return new TreeMap<>();
        }
        JsonArray jumpsArray = result.get("result").getAsJsonObject().get("jumps").getAsJsonArray();
        if (jumpsArray == null || jumpsArray.size() < 2) {
            return new TreeMap<>();
        }

        Map<Integer, CarrierJump> routeMap = new TreeMap<>();
        int remainingFuel = fuelSupply;
        int jumpIndex = 1;

        for (int i = 1; i < jumpsArray.size(); i++) { // Start from index 1 to skip current position
            jumpIndex = jumpIndex + 1;
            JsonObject jumpJson = jumpsArray.get(i).getAsJsonObject();

            int fuelUsed = jumpJson.get("fuel_used").getAsInt();
            remainingFuel -= fuelUsed;

            CarrierJump jump = new CarrierJump();
            jump.setSystemName(jumpJson.get("name").getAsString());
            jump.setDistance(Math.round(jumpJson.get("distance").getAsDouble() * 10.0) / 10.0);
            jump.setFuelUsed(fuelUsed);
            jump.setRemainingFuel(remainingFuel);
            jump.setHasIcyRing(jumpJson.get("has_icy_ring").getAsBoolean());
            jump.setPristine(jumpJson.get("is_system_pristine").getAsBoolean());
            jump.setX(jumpJson.get("x").getAsDouble());
            jump.setY(jumpJson.get("y").getAsDouble());
            jump.setZ(jumpJson.get("z").getAsDouble());
            jump.setLeg(jumpIndex);

            routeMap.put(jumpIndex, jump);
        }

        return routeMap;
    }

    record Request(String query) implements StringQuery {

        @Override public String getQuery() {
            return query;
        }
    }
}