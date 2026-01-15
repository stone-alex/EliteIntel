package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_MISSIONS;

public class AnalyzeMissionHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final MissionManager missionManager = MissionManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        MissionType[] availableMissionType = missionManager.getAvailableMissionType();
        if (availableMissionType.length == 0) return process("We have no active missions");

        Map<MissionType, Collection<MissionDto>> missions = Arrays.stream(availableMissionType).collect(
                Collectors.toMap(
                        missionType -> missionType,
                        missionType -> missionManager.getMissions(missionType).values(),
                        (a, b) -> b
                )
        );
        return process(new AiDataStruct(ANALYZE_MISSIONS.getInstructions(), new DataDto(missions)), originalUserInput);
    }

    record DataDto(Map<MissionType, Collection<MissionDto>> missions) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}