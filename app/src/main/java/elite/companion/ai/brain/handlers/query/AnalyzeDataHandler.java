package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.JsonDataFactory;
import elite.companion.util.json.ToJsonConvertible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AnalyzeDataHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeDataHandler.class);
    private static final Gson GSON = GsonFactory.getGson();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        QueryActions query = findQuery(action);
        String dataJsonStr = fetchDataForAction(query);
        if (dataJsonStr == null || dataJsonStr.isEmpty()) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }


        return analyzeData(dataJsonStr, originalUserInput);
    }

    private String fetchDataForAction(QueryActions action) {
        PlayerSession playerSession = PlayerSession.getInstance();

        //TODO: Ever growing case switch. A subject for refactoring.
        //NOTE: Consider either braking these into individual handlers, or implement data factory.
        return switch (action) {
            case QUERY_SEARCH_SIGNAL_DATA -> {
                Object signals = playerSession.getSignals();
                yield signals != null ? GSON.toJson(String.valueOf(signals)) : null;
            }
            case QUERY_SHIP_LOADOUT -> {
                Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT_JSON);
                yield loadout != null ? String.valueOf(loadout) : null;
            }
            case QUERY_NEXT_STAR_SCOOPABLE -> {
                Collection<? extends ToJsonConvertible> route = playerSession.getRoute().values();
                yield JsonDataFactory.getInstance().toJsonArrayString(route);
            }
            case QUERY_ANALYZE_ON_BOARD_CARGO -> {
                Object cargo = playerSession.get(PlayerSession.SHIP_CARGO);
                yield cargo != null ? GSON.toJson(cargo) : null;
            }
            case CHECK_LEGAL_STATUS -> {
                Object status = playerSession.get(PlayerSession.CURRENT_STATUS);
                yield status != null ? GSON.toJson(status) : null;
            }
            case QUERY_CARRIER_STATS -> {
                Object stats = playerSession.get(PlayerSession.CARRIER_STATS);
                yield stats != null ? GSON.toJson(stats) : null;
            }
            case ANALYZE_SCAN -> {
                Object scan = playerSession.get(PlayerSession.LAST_SCAN);
                yield scan != null ? GSON.toJson(scan) : null;
            }
            case ANALYZE_STELLAR_OBJECTS -> {
                Collection<? extends ToJsonConvertible> data = playerSession.getStellarObjects().values();
                yield GSON.toJson(data);
            }
            default -> null;
        };
    }
}