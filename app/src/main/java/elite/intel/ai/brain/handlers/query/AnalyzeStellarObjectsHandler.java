package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.LocationDto.LocationType;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnalyzeStellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing stelar objects data. Stand by."));

        StellarObjectsData<List<LocationData>, String> data = toLocationList(locationManager.findAllBySystemAddress(playerSession.getLocationData().getSystemAddress()));

        String instructions = """
                Answer ONLY the specific question asked. Do not give an overview or summary unless the user explicitly asks for one.
                Data fields:
                - summary: pre-computed counts (stars, planets, moons, stations, landable, bio signals, scoopable stars). Use this for any count or summary question.
                - detailedStellarObjectList: full list of stellar objects with per-object data:
                  - stellarObjectName: short display name
                  - objectClass: STAR, PLANET, MOON, STATION
                  - objectType: specific type (e.g. Rocky Body, High metal content world, Neutron Star)
                  - starClass: star spectral class (M, K, G, F, A, B, O are fuel-scoopable)
                  - isLandable: whether the surface can be landed on
                  - isTerraformable: terraforming candidate
                  - gravity: surface gravity (zero means no data)
                  - surfaceTemperature: in Celsius
                  - atmosphere: atmosphere type or None
                  - parentPlanetName: parent body if this is a moon
                  - distanceFromStar: distance from primary star in light seconds
                  - bioSignals: number of biological signals detected
                  - ourDiscovery: true if we were first to discover this body
                  - weMappedIt: true if we mapped this body
                  - hasMarkets: true if this location has a market
                
                Rules:
                - For count or summary questions: use summary directly. Do not recount from the list.
                - For specific object questions: search detailedStellarObjectList by stellarObjectName or objectClass.
                - Do not invent data not present in the provided fields.
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

    private StellarObjectsData<List<LocationData>, String> toLocationList(Collection<LocationDto> locations) {

        ArrayList<LocationData> result = new ArrayList<>();
        int numberOfMoons = 0;
        int numberOfPlanets = 0;
        int numberOfStars = 0;
        int numberOfStations = 0;
        for (LocationDto location : locations) {
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
                    "UNKNOWN".equals(locationType.name()) ? "" : locationType.name(),
                    isPlanetaryRing ? "Planetary Ring" : location.getPlanetClass(),
                    location.getStarClass(),
                    location.getStarName(),
                    location.isLandable(),
                    location.isTerraformable(),
                    Math.round(location.getGravity()),
                    Math.round(( location.getSurfaceTemperature() - 273 ) ), // Convert Kelvin to Celsius
                    location.getAtmosphere(),
                    location.getParentBodyName(),
                    Math.round(location.getDistance()),
                    location.getBioSignals(),
                    location.isOurDiscovery(),
                    location.isWeMappedIt(),
                    location.getMarket() != null
            ));
        }

        long landableMoons = result.stream().filter(l -> "MOON".equals(l.objectClass()) && l.isLandable()).count();
        long landablePlanets = result.stream().filter(l -> "PLANET".equals(l.objectClass()) && l.isLandable()).count();
        long bioMoons = result.stream().filter(l -> "MOON".equals(l.objectClass()) && l.bioSignals() > 0).count();
        long bioPlanets = result.stream().filter(l -> "PLANET".equals(l.objectClass()) && l.bioSignals() > 0).count();
        long atmosMoons = result.stream().filter(l -> "MOON".equals(l.objectClass()) && hasAtmosphere(l)).count();
        long fuelStars = result.stream().filter(l -> isScoopable(l)).count();

        String summary = """
                Star System contains: %d stars, %d planets, %d moons, %d stations.
                PRE-COMPUTED FACTS (authoritative, do not recount from the detail list):
                Landable moons: %d
                Landable planets: %d
                Moons with bio signals: %d
                Planets with bio signals: %d
                Moons with atmosphere: %d
                Scoopable fuel stars: %d
                """.formatted(numberOfStars, numberOfPlanets, numberOfMoons, numberOfStations,
                landableMoons, landablePlanets, bioMoons, bioPlanets, atmosMoons, fuelStars);

        return new StellarObjectsData<>(result, summary);
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
                        String parentPlanetName,
                        double distanceFromStar,
                        int bioSignals,
                        boolean ourDiscovery,
                        boolean weMappedIt,
                        boolean hasMarkets
    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record DataDto(String summary, List<LocationData> detailedStellarObjectList) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
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


    private static boolean hasAtmosphere(LocationData l) {
        String atm = l.atmosphere();
        return atm != null && !atm.isBlank() && !"None".equalsIgnoreCase(atm);
    }

    private static boolean isScoopable(LocationData l) {
        String sc = l.starClass();
        return sc != null && List.of("M", "K", "G", "F", "A", "B", "O").contains(sc.trim().toUpperCase());
    }
}
