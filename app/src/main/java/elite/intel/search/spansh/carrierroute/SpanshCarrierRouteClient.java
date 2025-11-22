package elite.intel.search.spansh.carrierroute;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * Client for querying spansh.co.uk fleet carrier routing API.
 * Handles POST to queue a route job and immediate GET for results, with custom fuel calculation.
 */
public class SpanshCarrierRouteClient {

    private static final Logger log = LogManager.getLogger(SpanshCarrierRouteClient.class);
    private static final String BASE_URL = "https://spansh.co.uk/api/fleetcarrier/route";
    private static final String RESULTS_URL = "https://spansh.co.uk/api/results/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Calculates a fleet carrier route based on criteria and current fuel supply.
     *
     * @param criteria   Search criteria for the route.
     * @param fuelSupply Current fuel level in tons.
     * @return Map of jump index (1-based) to CarrierJump details, or empty map on failure.
     */
    public Map<Integer, CarrierJump> calculateRoute(CarrierRouteCriteria criteria, int fuelSupply) {
        try {
            // Build form-urlencoded payload
            String payload = "source=" + URLEncoder.encode(criteria.sourceSystem(), StandardCharsets.UTF_8) +
                    "&destinations=" + URLEncoder.encode(criteria.destinationSystem(), StandardCharsets.UTF_8) +
                    "&capacity=" + criteria.capacity() +
                    "&capacity_used=" + criteria.capacityUsed() +
                    "&calculate_starting_fuel=" + criteria.calculateStartingFuel();

            // POST to queue job
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
            if (postResponse.statusCode() != 202) {
                log.warn("POST to route failed with status: {}", postResponse.statusCode());
                return new TreeMap<>();
            }

            Thread.sleep(10000);

            JsonObject postJson = gson.fromJson(postResponse.body(), JsonObject.class);
            String jobId = postJson.get("job") != null ? postJson.get("job").getAsString() : null;
            if (jobId == null || jobId.isEmpty()) {
                log.warn("Invalid or missing job ID in POST response");
                return new TreeMap<>();
            }

            // GET results immediately (no polling needed)
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(RESULTS_URL + jobId))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (getResponse.statusCode() != 200) {
                log.warn("GET to results/{} failed with status: {}", jobId, getResponse.statusCode());
                return new TreeMap<>();
            }

            JsonObject getJson = gson.fromJson(getResponse.body(), JsonObject.class);
            JsonObject resultJson = getJson.getAsJsonObject("result");
            if (resultJson == null) {
                log.warn("No result object in GET response for job {}", jobId);
                return new TreeMap<>();
            }

            // Process jumps, skipping the first (current position)
            JsonArray jumpsArray = resultJson.getAsJsonArray("jumps");
            if (jumpsArray == null || jumpsArray.size() < 2) {
                log.warn("No valid jumps in results for job {}", jobId);
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
        } catch (IOException | InterruptedException e) {
            log.warn("Carrier route calculation failed: {}", e.getMessage());
            return new TreeMap<>();
        }
    }
}