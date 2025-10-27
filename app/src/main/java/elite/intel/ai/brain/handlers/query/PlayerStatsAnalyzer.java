package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_PLAYER_STATS_ANALYSIS;

/**
 * The PlayerStatsAnalyzer class is responsible for handling and analyzing player statistics queries.
 * It extends the BaseQueryAnalyzer class to leverage core query analysis functionalities
 * and implements the QueryHandler interface to ensure compliance with standard query processing requirements.
 * <p>
 * Responsibilities:
 * - Processes player statistics-related queries by analyzing the relevant data.
 * - Validates the integrity of player statistics data before performing any analysis.
 * - Delegates specific data analysis tasks to the functionality provided by the BaseQueryAnalyzer.
 * <p>
 * Key Methods:
 * - handle: Processes incoming actions, retrieves corresponding player statistics, validates the data,
 * and returns the analyzed result based on the query's requirements.
 * <p>
 * Logging:
 * The class makes use of SLF4J for logging error scenarios, such as invalid data or other processing issues.
 * <p>
 * Behavior:
 * - If the required stats data (extracted from RankAndProgressDto) is unavailable, it returns an error message.
 * - Ensures that JSON data following serialization is valid before proceeding with analysis.
 * - Utilizes BaseQueryAnalyzer to perform actionable steps, such as data analysis through AI interfaces.
 */
public class PlayerStatsAnalyzer extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LogManager.getLogger(PlayerStatsAnalyzer.class);


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        Queries query = findQuery(action);
        RankAndProgressDto data = PlayerSession.getInstance().getRankAndProgressDto();

        if (data == null) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        String dataJsonStr = JsonDataFactory.getInstance().toJsonString(data);

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return process(new DataDto(QUERY_PLAYER_STATS_ANALYSIS.getInstructions(), dataJsonStr), originalUserInput);
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
