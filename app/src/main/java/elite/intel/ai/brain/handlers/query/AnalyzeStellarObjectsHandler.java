package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.LocationDto.LocationType;
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
        StellarObjectsData<List<LocationData>, String> data = toLocationList(playerSession.getLocations());

        String instructions = """
                You are a strict data-only responder. Use ONLY the provided JSON array "allStellarObjectsInStarSystem" and "summary" field.
                The 'summary' field provides general summary of the star system. (number of stars, planets, moons and stations)
                Examples of good short answers (reference only – do NOT copy literally):
                - "Yes, '1 b' is landable, rocky body, 0.13 g, CO₂ atmosphere."
                - "Landable rocky bodies: 1 b, 1 c, 3 d, 2 d a"
                - "Bodies with bio signals: '1 b' (3 signals)"
                - "Star system has two stars, three planets, and one moon."
                - "No bodies with rings in this system."
                """;

        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                data.getSummary(),
                                data.getObjectList()
                        )
                ),
                originalUserInput
        );
    }

    private StellarObjectsData<List<LocationData>, String> toLocationList(Map<Long, LocationDto> locations) {
        Collection<LocationDto> values = locations.values();
        ArrayList<LocationData> result = new ArrayList<>();
        int numberOfMoons = 0;
        int numberOfPlanets = 0;
        int numberOfStars = 0;
        int numberOfStations = 0;
        for (LocationDto location : values) {
            boolean isPlanetaryRing = location.getPlanetName().contains("Ring");
            LocationType locationType = location.getLocationType();

            if (LocationType.STAR == locationType || LocationType.PRIMARY_STAR == locationType) {
                numberOfStars++;
            }

            if (LocationType.PLANET == locationType){
                numberOfPlanets++;
            }

            if(LocationType.MOON == locationType){
                numberOfMoons++;
            }

            if(LocationType.STATION == locationType){
                numberOfStations++;
            }

            result.add(new LocationData(
                    location.getPlanetShortName(),
                    locationType.name(),
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
                    location.getParentBodyName(),
                    Math.round(location.getDistance()),
                    location.getBioSignals(),
                    location.isOurDiscovery(),
                    location.isWeMappedIt(),
                    location.getMarket() != null
            ));
        }
        return new StellarObjectsData<>(result, "Star System contains: " + numberOfStars + " stars " + numberOfPlanets + " planets " + numberOfMoons + " moons, and " + numberOfStations + " stations.");
    }

    record LocationData(String stellarObjectName,
                        String objectClass,
                        String objectType,
                        String starClass,
                        String starName,
                        boolean isLandable,
                        boolean isTerraformable,
                        double gravity,
                        double surfaceTemperature,
                        String atmosphere,
                        String volcanism,
                        boolean isPlanetaryRing,
                        String parentPlanetName,
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

    record DataDto(String summary, List<LocationData> allStellarObjectsInStarSystem) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    private static class StellarObjectsData<A, B> {
        private final A locationData;
        private final B summary;

        public StellarObjectsData(A list, B string) {
            locationData = list;
            summary = string;
        }

        public A getObjectList() {
            return locationData;
        }

        public B getSummary() {
            return summary;
        }
    }
}
