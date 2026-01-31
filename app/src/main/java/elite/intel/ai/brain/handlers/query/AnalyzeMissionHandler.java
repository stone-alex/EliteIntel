package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.LocationManager;
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
    private final LocationManager locationManager = LocationManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

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

        LocationDto playerLocation = locationManager.findByLocationData(playerSession.getLocationData());
        String instructions = """
                        Use this data to answer questions about outstanding (incomplete/active) missions.
                
                        Follow these rules in order of priority:
                
                        1. If the question does NOT mention any specific star system or location → return the full list of missions, including for each: faction, missionDescription, destinationSystem (and origin system if relevant).
                
                        2. If the question mentions the current star system (or "here", "current location", "this system", etc.) → return ONLY missions that match currentStarSystem == originSystem (or pickup location) AND/OR currentStarSystem == destinationSystem. Clearly state if no missions match the current system.
                
                        3. If the question mentions a different/specific system (not the current one) → return only missions relevant to that named system (as origin or destination).
                
                        Never assume the user is only asking about the current system unless the question explicitly refers to it.
                        
                        Do NOT filter to current system unless the question explicitly refers to the current location / current system / here / local missions.
                """;
        return process(new AiDataStruct(instructions, new DataDto(missions, playerLocation.getStarName())), originalUserInput);
    }

    record DataDto(Map<MissionType, Collection<MissionDto>> missions, String currentStarSystem) implements ToJsonConvertible {
        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}