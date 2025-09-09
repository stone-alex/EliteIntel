package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.DeathsDto;
import elite.companion.ai.search.api.dto.TrafficDto;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.JsonDataFactory;
import elite.companion.util.json.ToJsonConvertible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

public class AnalyzeRouterHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeRouterHandler.class);
    private static final Gson GSON = GsonFactory.getGson();


    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new VoiceProcessEvent("Analyzing route..."));

        QueryActions query = findQuery(action);
        PlayerSession playerSession = PlayerSession.getInstance();

        Collection<ToJsonConvertible> route = new LinkedHashSet<>();
        Map<String, NavRouteDto> plottedRoute = playerSession.getRoute();
        for (NavRouteDto dto : plottedRoute.values()) {
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
