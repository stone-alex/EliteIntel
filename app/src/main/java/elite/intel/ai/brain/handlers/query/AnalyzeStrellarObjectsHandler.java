package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.SystemBodiesDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnalyzeStrellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    /// TODO: break this down to several handlers. The data set is too large for local LLM.
    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing star system data... Stand by..."));
        String primaryStarName = playerSession.getPrimaryStarName();

        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(primaryStarName);
        List<LocationDto> allLocations = toLocationList(playerSession.getLocations());


        String instructions = """
                You are a strict data-only responder. Use ONLY the provided JSON array "allStellarObjectsInStarSystem".
                
                Rules – follow in exact order:
                
                1. The array contains one object per body (star, planet, moon).
                   Key fields to use:
                   - planetName / planetShortName: full/short name of the body
                   - locationType: PRIMARY_STAR or PLANET_OR_MOON
                   - planetClass: e.g. "Sudarsky class III gas giant", "Rocky body", "Gas giant with ammonia based life"
                   - isLandable: true/false
                   - isTerraformable: true/false
                   - gravity: surface gravity in g
                   - surfaceTemperature: in K
                   - atmosphere: e.g. "CarbonDioxide", "None", "SulphurDioxide"
                   - volcanism: e.g. "minor metallic magma volcanism", "" (empty = none)
                   - hasRings: true/false
                   - distance: from primary star in ls
                   - bioSignals: number of biological signals (if present and >0)
                   - detectedSignals: array of any other signals (usually empty in this data)
                   - ourDiscovery / weMappedIt: true if we discovered/mapped it
                
                2. Answer concisely and factually using ONLY data from the array.
                   - If the question is about existence/presence → list matching bodies with short name + relevant field(s)
                   - If asking for a list (e.g. "landable bodies", "gas giants", "bodies with bio signals") → give short bullet-style list of short names + key property
                   - If asking for details on specific body → give only the asked fields for that exact planetName / planetShortName
                   - If no matching bodies → answer "None in this system" or "No bodies match that criteria"
                   - Never guess, never add bodies not in the array, never use external knowledge
                
                3. response_text must be short (1–3 sentences max), no enthusiasm, no chit-chat.
                
                Examples of good short answers (reference only – do NOT copy literally):
                - "Yes, '1 b' is landable, rocky body, 0.13 g, CO₂ atmosphere."
                - "Landable rocky bodies: 1 b, 1 c, 1 d, 1 d a, 1 e, 1 e a, 2 a, 2 b, 2 c, 2 d"
                - "Bodies with bio signals: '1 b' (3 signals)"
                - "No bodies with rings in this system."
                """;

        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                allLocations,
                                allLocations.size() == 0 ? edsmData : null
                        )
                ),
                originalUserInput
        );
    }

    private List<LocationDto> toLocationList(Map<Long, LocationDto> locations) {
        Collection<LocationDto> values = locations.values();
        ArrayList<LocationDto> result = new ArrayList<>();
        for (LocationDto filtered : values) {
            filtered.setTrafficDto(null);
            filtered.setDeathsDto(null);
            filtered.setFssSignals(null);
            filtered.setGenus(null);
            filtered.setLandingCoordinates(null);
            filtered.setMaterials(null);
            filtered.setStationServices(null);
            filtered.setSaaSignals(null);
            filtered.setPartialBioSamples(null);
            result.add(filtered);
        }
        return result;
    }


    record DataDto(
            List<LocationDto> allStellarObjectsInStarSystem,
            SystemBodiesDto edsmData
    ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
