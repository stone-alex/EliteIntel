package elite.intel.ai.search.spansh.market;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Client for querying spansh.co.uk stations API.
 * Handles POST to save search, GET to recall results, and filters for target commodity.
 */
public class SpanshMarketClient {

    private static final String BASE_URL = "https://spansh.co.uk/api/stations/search";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Searches for stations based on criteria in the provided record.
     *
     * @param criteria Search criteria including reference system, distance, commodity, etc.
     * @return List of StationResult with relevant details.
     * @throws IOException          If network or parse error.
     * @throws InterruptedException If request interrupted.
     */
    public List<StationMarket> searchMarkets(MarketSearchCriteria criteria) throws IOException, InterruptedException {

        // Build POST payload
        JsonObject filters = new JsonObject();
        JsonObject distance = new JsonObject();
        distance.addProperty("min", criteria.minDistance());
        distance.addProperty("max", criteria.maxDistance());
        filters.add("distance", distance);

        JsonArray market = new JsonArray();
        JsonObject commodity = new JsonObject();
        commodity.addProperty("name", criteria.commodityName());
        market.add(commodity);
        filters.add("market", market);

        JsonObject payload = new JsonObject();
        payload.add("filters", filters);
        payload.add("sort", new JsonArray());
        payload.addProperty("page", 1); // one page
        payload.addProperty("size", 100); // 100 results per page
        payload.addProperty("reference_system", criteria.referenceSystem());

        // POST to save search
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/save"))
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        if (postResponse.statusCode() != 200) {
            throw new IOException("POST failed: " + postResponse.statusCode());
        }

        JsonObject postJson = gson.fromJson(postResponse.body(), JsonObject.class);
        String searchReference = postJson.get("search_reference").getAsString();

        // GET to recall results
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/recall/" + searchReference))
                .header("Content-Type", "application/json;charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        if (getResponse.statusCode() != 200) {
            throw new IOException("GET failed: " + getResponse.statusCode());
        }

        JsonObject getJson = gson.fromJson(getResponse.body(), JsonObject.class);

        // Parse and filter results
        JsonArray resultsArray = getJson.getAsJsonArray("results");
        List<StationMarket> stations = new ArrayList<>();

        for (var stationElement : resultsArray) {
            JsonObject stationJson = stationElement.getAsJsonObject();

            // Apply station-level filters
            if (criteria.requireLargePad() && !stationJson.get("has_large_pad").getAsBoolean()) {
                continue;
            }
            if (criteria.requirePlanetary() != null &&
                    criteria.requirePlanetary() != stationJson.get("is_planetary").getAsBoolean()) {
                continue;
            }

            // Check market for the commodity
            JsonArray marketArray = stationJson.getAsJsonArray("market");
            if (marketArray != null) {
                for (var itemElement : marketArray) {
                    JsonObject item = itemElement.getAsJsonObject();
                    if (
                            criteria.commodityName().equalsIgnoreCase(item.get("commodity").getAsString())
                                    && (!criteria.requireSupply() || item.get("supply").getAsInt() > 0)
                                    && (criteria.wantToBuy() ? item.get("sell_price").getAsInt() > 0 : item.get("buy_price").getAsInt() > 0)
                                    && (criteria.minSupply() <= 0 || item.get("supply").getAsInt() >= criteria.minSupply())
                    ) {

                        StationMarket station = new StationMarket();
                        station.setStationName(stationJson.get("name").getAsString());
                        station.setSystemName(stationJson.get("system_name").getAsString());
                        station.setDistance(Math.round(stationJson.get("distance").getAsDouble() * 10.0) / 10.0);
                        station.setHasLargePad(stationJson.get("has_large_pad").getAsBoolean());
                        station.setPlanetary(stationJson.get("is_planetary").getAsBoolean());
                        station.setMarketUpdatedAt(stationJson.get("market_updated_at").getAsString());
                        station.setSellPrice(item.get("sell_price").getAsInt());
                        station.setBuyPrice(item.get("buy_price").getAsInt());
                        station.setCommoditySupply(item.get("supply").getAsInt());
                        stations.add(station);
                        break; // Found the commodity, no need to check further
                    }
                }
            }
        }

        if (criteria.orderByDistance())
            stations.sort(Comparator.comparing(StationMarket::distance));
        else if(criteria.wantToBuy())
            stations.sort(Comparator.comparing(StationMarket::getSellPrice));
        else
            stations.sort(Comparator.comparing(StationMarket::getBuyPrice));

        return stations;
    }
}