package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnalyzeStellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing stelar objects data... Stand by..."));
        List<LocationData> allLocations = toLocationList(playerSession.getLocations());

        String instructions = """
                You are a strict data-only responder. Use ONLY the provided JSON array "allStellarObjectsInStarSystem".
                
                Examples of good short answers (reference only – do NOT copy literally):
                - "Yes, '1 b' is landable, rocky body, 0.13 g, CO₂ atmosphere."
                - "Landable rocky bodies: 1 b, 1 c, 3 d, 2 d a"
                - "Bodies with bio signals: '1 b' (3 signals)"
                - "No bodies with rings in this system."
                """;

        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                allLocations
                        )
                ),
                originalUserInput
        );
    }

    private List<LocationData> toLocationList(Map<Long, LocationDto> locations) {
        Collection<LocationDto> values = locations.values();
        ArrayList<LocationData> result = new ArrayList<>();
        for (LocationDto location : values) {
            boolean isPlanetaryRing = location.getPlanetName().contains("Ring");
            result.add(new LocationData(
                    location.getPlanetShortName(),
                    isPlanetaryRing ? "Planetary Ring" : location.getPlanetClass(),
                    location.getStarClass(),
                    location.getStarName(),
                    location.isLandable(),
                    location.isTerraformable(),
                    Math.round(location.getGravity()),
                    Math.round(location.getSurfaceTemperature()),
                    location.getAtmosphere(),
                    location.getVolcanism(),
                    isPlanetaryRing,
                    Math.round(location.getDistance()),
                    location.getBioSignals(),
                    location.isOurDiscovery(),
                    location.isWeMappedIt(),
                    location.getMarket() != null
            ));
        }
        return result;
    }

    record LocationData(String stellarObjectName,
                        String objectClass,
                        String starClass,
                        String starName,
                        boolean isLandable,
                        boolean isTerraformable,
                        double gravity,
                        double surfaceTemperature,
                        String atmosphere,
                        String volcanism,
                        boolean isPlanetaryRing,
                        double distance,
                        int bioSignals,
                        boolean ourDiscovery,
                        boolean weMappedIt,
                        boolean hasMarkets
    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto(List<LocationData> allStellarObjectsInStarSystem) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
