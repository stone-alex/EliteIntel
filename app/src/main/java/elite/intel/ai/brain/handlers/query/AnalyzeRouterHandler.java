package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.session.PlayerSession;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_ANALYZE_ROUTE;

public class AnalyzeRouterHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Logger log = LogManager.getLogger(AnalyzeRouterHandler.class);

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing route telemetry... Stand By..."));
        Queries query = findQuery(action);

        ShipRouteManager shipRoute = ShipRouteManager.getInstance();

        Collection<ToJsonConvertible> route = new LinkedHashSet<>();
        Collection<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        for (NavRouteDto dto : orderedRoute) {
            if (dto.getDeathData() == null) {
                DeathsDto deathsDto = EdsmApiClient.searchDeaths(dto.getName());
                if (deathsDto.getData().getDeaths().getTotal() > 0) {
                    dto.setDeathData(deathsDto);
                    route.add(deathsDto);
                }
            }

            if (dto.getTraffic() == null) {
                TrafficDto trafficDto = EdsmApiClient.searchTraffic(dto.getName());
                if (trafficDto.getData().getTraffic().getTotal() > 0) {
                    dto.setTraffic(trafficDto);
                    route.add(trafficDto);
                }
            }

            route.add(dto);
            shipRoute.updateRouteNode(dto);
        }

        String data = JsonDataFactory.getInstance().toJsonArrayString(route);

        if (data == null || data.isEmpty()) {
            return GenericResponse.getInstance().genericResponse("No data available...");
        }

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(data)) {
            log.error("Invalid data JSON for query {}: {}", query, data);
            return GenericResponse.getInstance().genericResponse("Data error!");
        }

        return process(new AiDataStruct(QUERY_ANALYZE_ROUTE.getInstructions(), new DataDto(data)), originalUserInput);
    }

    record DataDto(String data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
