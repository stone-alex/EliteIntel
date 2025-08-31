package elite.companion.comms.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.GsonFactory;
import elite.companion.util.JsonDataFactory;
import elite.companion.util.ToJsonConvertible;
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

/*
    private QueryActions findQuery(String action) {
        for (QueryActions qa : QueryActions.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        throw new IllegalArgumentException("No query action found for: " + action);
    }
*/

    private String fetchDataForAction(QueryActions action) {
        SystemSession systemSession = SystemSession.getInstance();
        PlayerSession playerSession = PlayerSession.getInstance();

        return switch (action) {
            case QUERY_SEARCH_SIGNAL_DATA -> {
                Object signals = systemSession.getSignals();
                yield signals != null ? GSON.toJson(String.valueOf(signals)) : null;
            }
            case QUERY_SHIP_LOADOUT -> {
                Object loadout = systemSession.get(SystemSession.SHIP_LOADOUT_JSON);
                yield loadout != null ? String.valueOf(loadout) : null;
            }
            case QUERY_ANALYZE_ROUTE, QUERY_NEXT_STAR_SCOOPABLE -> {
                Collection<? extends ToJsonConvertible> route = systemSession.getRoute().values();
                yield JsonDataFactory.getInstance().toJsonArrayString(route);
            }
            case QUERY_ANALYZE_ON_BOARD_CARGO -> {
                Object cargo = systemSession.get(SystemSession.SHIP_CARGO);
                yield cargo != null ? GSON.toJson(cargo) : null;
            }
            case LOCAL_SYSTEM_INFO -> {
                Object system = systemSession.get(SystemSession.CURRENT_SYSTEM);
                yield system != null ? GSON.toJson(system) : null;
            }
            case CHECK_LEGAL_STATUS -> {
                Object status = systemSession.get(SystemSession.CURRENT_STATUS);
                yield status != null ? GSON.toJson(status) : null;
            }
            case QUERY_CARRIER_STATS -> {
                Object stats = playerSession.get(PlayerSession.CARRIER_STATS);
                yield stats != null ? GSON.toJson(stats) : null;
            }
            default -> null;
        };
    }
}