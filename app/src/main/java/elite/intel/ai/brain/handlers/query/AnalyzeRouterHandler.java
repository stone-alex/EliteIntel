package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.JsonDataFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;

public class AnalyzeRouterHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Logger log = LogManager.getLogger(AnalyzeRouterHandler.class);


    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new VocalisationRequestEvent("Analyzing route..."));

        QueryActions query = findQuery(action);
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

        String jsonArrayAsString = JsonDataFactory.getInstance().toJsonArrayString(route);

        if (jsonArrayAsString == null || jsonArrayAsString.isEmpty()) {
            return GenericResponse.getInstance().genericResponse("No data available...");
        }

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(jsonArrayAsString)) {
            log.error("Invalid data JSON for query {}: {}", query, jsonArrayAsString);
            return GenericResponse.getInstance().genericResponse("Data error!");
        }


        return analyzeData(jsonArrayAsString, originalUserInput);

    }
}
