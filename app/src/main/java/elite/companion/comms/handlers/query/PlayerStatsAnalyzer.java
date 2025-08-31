package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.SystemSession;
import elite.companion.util.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerStatsAnalyzer extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(PlayerStatsAnalyzer.class);


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        QueryActions query = findQuery(action);
        RankDto data = (RankDto) SystemSession.getInstance().get(SystemSession.RANK);

        if (data == null) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        String dataJsonStr = JsonDataFactory.getInstance().toJsonString(data);

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return analyzeData(dataJsonStr, originalUserInput);

    }
}
