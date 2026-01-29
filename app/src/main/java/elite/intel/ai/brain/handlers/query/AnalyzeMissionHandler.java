package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyzeMissionHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final MissionManager missionManager = MissionManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        MissionType[] availableMissionTypes = missionManager.getAvailableMissionTypes();
        if (availableMissionTypes.length == 0) return process("We have no active missions");

        Map<MissionType, Collection<MissionDto>> missions = Arrays.stream(availableMissionTypes).collect(
                Collectors.toMap(
                        missionType -> missionType,
                        missionType -> missionManager.getMissions(missionType).values(),
                        (a, b) -> b
                )
        );

        LocationDto playerLocation = PlayerSession.getInstance().getCurrentLocation(); // Provide the llm with current system
        String instructions = """
                        Use this map of missionType to mission to answer questions about active missions.
                        If the question is generally open, group the results by missionType.
                        When specifically prompted about an empty list, respond with 'I have no data for this category'.
                        Keep the response short, concise and straight to the point.
                """
                + "If the user asks about the current system, only use the relevant data for this system: '" + playerLocation.getStarName() + "'";
        return process(new AiDataStruct(instructions, new DataDto(missions)), originalUserInput);
    }

    record DataDto(Map<MissionType, Collection<MissionDto>> missions) implements ToJsonConvertible {
        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}