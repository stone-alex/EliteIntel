package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
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
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing route..."));

        Queries query = findQuery(action);
        PlayerSession playerSession = PlayerSession.getInstance();

        Collection<ToJsonConvertible> route = new LinkedHashSet<>();
        Collection<NavRouteDto> orderedRoute = playerSession.getOrderedRoute();
        for (NavRouteDto dto : orderedRoute) {
            DeathsDto deathsDto = EdsmApiClient.searchDeaths(dto.getName());
            Thread.sleep(500);
            TrafficDto trafficDto = EdsmApiClient.searchTraffic(dto.getName());
            Thread.sleep(500);
            route.add(dto);
            if (deathsDto.getData().getDeaths().getTotal() > 0) route.add(deathsDto);
            if (trafficDto.getData().getTraffic().getTotal() > 0) route.add(trafficDto);
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

        return process(new DataDto(QUERY_ANALYZE_ROUTE.getInstructions(), data), originalUserInput);
    }

    record DataDto(String instructions, String data) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}
