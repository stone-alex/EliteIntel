package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.search.edsm.dto.data.DeathsStats;
import elite.intel.search.edsm.dto.data.TrafficStats;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    public static final double DAY = 86400.0;
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing current location data... Stand by..."));
        Status status = Status.getInstance();

        LocationDto location = locationManager.findByLocationData(playerSession.getLocationData());
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(location.getStarName());
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(location.getStarName());

        String station = "Station";
        if (status.isDocked() && location.getStationName() != null || location.getStationName() != null) {
            station = "Docked at " + location.getStationName() + " " + location.getStationType();
        }

        String instructions = """                
                    The user may ask multiple questions at once. Answer each one individually using the matching rule below. Do not combine them into one sentence unless natural. Do not say "Insufficient data" if the field exists for part of the question.
                    - IF asked for summary or broad 'where are we' question return starSystemName, planetName followed by summary of what data provided. Example: Star System <starSystemName>, Planet <planetName>. - <summary>
                    - IF planetName is unknown check stationName. Example: Docked at <stationName> in star system <starSystemName>
                    - Extract and answer ALL questions in the user input using ONLY the provided data fields.
                    - For temperature: If temperature is in data (in Kelvin), convert to Celsius and say: "Temperature on <planetName> is <X> degrees Celsius."
                    - For day length: Use dayLength directly and say: "Day on <planetName> lasts <dayLength>"
                    - Answer each requested piece of information separately and clearly.
                    - If any requested info is missing or not in data, omit that part only.
                
                    Use this data to provide answers for our location.
                    - IF asked 'where are we?' Use planetShortName for location name unless we are on the station in which case return stationName.
                    - IF Asked about Temperature: Temperature data is provided in surfaceTemperatureInKelvin (Kelvin), covert to Celsius and announce Celsius. Example: Temperature on <planetName> is <X> degrees Celsius.
                    - IF temperature is higher than 1000, current location is star system, the temp is of that of the local star. Do not mention temperature in this case.
                    - IF Asked about Length Of The Day: Use dayLength value. Example: Day on <planetName> lasts <X> hours and <Y> minutes
                    - IF Asked about Local Government, Controlling Powers, Controlling Faction, and localPowers/controllingFaction data is not present, the planet is uninhabited - ELSE use this data for your answer. 
                    Example 1: <planetName> is uninhabited. Or <planetName> is controlled by <X> powers and controlling faction is <Y>
                    Example 2: We are Docked at <station> in <starSystemName> star system. Medium Security. Deaths total X week Y day Z. Traffic total X week Y day Z. Day length is X.
                """;

        double rotationPeriod = Math.round(location.getRotationPeriod() * 100.0) / 100.0;
        double surfaceTemperatureInKelvin = Math.round(location.getSurfaceTemperature() * 100.0) / 100.0;
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                location.getStarName(),
                                location.getPlanetShortName(),
                                location.getSecurity(),
                                station,
                                location.getStationFaction(),
                                location.getPowers() == null ? null : location.getPowers().toArray(String[]::new),
                                deathsDto.getData() == null ? null : deathsDto.getData().getDeaths(),
                                trafficDto.getData() == null ? null : trafficDto.getData().getTraffic(),
                                rotationPeriod,
                                location.getRadius(),
                                location.isTidalLocked(),
                                surfaceTemperatureInKelvin,
                                getFormattedSolarDayLength(location.getRotationPeriod(), location.getOrbitalPeriod(), location.isTidalLocked())
                        )
                ),
                originalUserInput
        );
    }

    private String getFormattedSolarDayLength(double rotationPeriodSeconds, double orbitalPeriodSeconds, boolean isTidallyLocked) {

        if (isTidallyLocked) {
            // For tidal lock: solar day = sidereal day (rotation period)
            double siderealAbs = Math.abs(rotationPeriodSeconds);
            return formatSecondsToHoursMinutes(siderealAbs);
        }

        if (orbitalPeriodSeconds <= 0) {
            // No orbit data → fallback to sidereal
            return formatSecondsToHoursMinutes(Math.abs(rotationPeriodSeconds));
        }

        double siderealAbs = Math.abs(rotationPeriodSeconds);
        if (siderealAbs < 60) {
            return "Unknown";
        }

        double siderealDays = siderealAbs / DAY;
        double orbitalDays = orbitalPeriodSeconds / DAY;

        double relativeSpeed;
        if (rotationPeriodSeconds < 0) {
            // retrograde: apparent day is shorter
            relativeSpeed = 1.0 / siderealDays + 1.0 / orbitalDays;
        } else {
            // prograde
            double diff = 1.0 / orbitalDays - 1.0 / siderealDays;
            relativeSpeed = Math.abs(diff);  // avoid negative
        }

        if (relativeSpeed < 1e-9) {
            // synchronous / near-lock
            return formatSecondsToHoursMinutes(siderealAbs);
        }

        double solarSeconds = DAY / relativeSpeed;

        // Safety cap: prevent absurd values (e.g. orbital period bug)
        if (solarSeconds > 1e10 || solarSeconds < 60) {
            return formatSecondsToHoursMinutes(siderealAbs);
        }

        return formatSecondsToHoursMinutes(solarSeconds);
    }

    // Helper – keeps code clean
    private String formatSecondsToHoursMinutes(double seconds) {
        if (seconds <= 0) return "Unknown";

        long totalSec = Math.round(seconds);
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;

        return String.format("%d hours and %d minutes", hours, minutes);
    }

    record DataDto(
            String starSystemName,
            String planetName,
            String securityLevel,
            String stationName,
            String controllingFaction,
            String[] localPowers,
            DeathsStats deathsData,
            TrafficStats trafficData,
            double rotationPeriod,
            double planetRadius,
            boolean isTidallyLocked,
            double surfaceTemperatureInKelvin,
            String dayLength

    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
