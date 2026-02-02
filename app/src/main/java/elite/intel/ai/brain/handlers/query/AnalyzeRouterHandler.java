package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.util.json.GsonFactory;
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
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing route telemetry... Stand By..."));
        ShipRouteManager shipRoute = ShipRouteManager.getInstance();
        Collection<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        String instructions = """
                Analyze the current plotted route.
                Number of jumps = number of nodes.
                    - If asked about next way point, that be the first node in the route. Return info about that node only (star class, remaining jumps, security info if any). Jumps remaining = number of nodes.
                """;
        return process(new AiDataStruct(instructions, new DataDto(orderedRoute)), originalUserInput);
    }

    record DataDto(Collection<NavRouteDto> data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
