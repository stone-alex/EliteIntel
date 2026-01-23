package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing current location data... Stand by..."));
        Status status = Status.getInstance();
        PlayerSession playerSession = PlayerSession.getInstance();


        LocationDto location = playerSession.getCurrentLocation();
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(playerSession.getPrimaryStarLocation().getStarName());
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(playerSession.getPrimaryStarLocation().getStarName());

        String station = "";
        if (status.isDocked() && location.getStationName() != null || location.getStationName() != null) {
            station = "Docked at " + location.getStationName() + " " + location.getStationType();
        }

        String instructions = """
                    Use this data to provide answers for our location. 
                    NOTE: For questions such as 'where are we?' 
                    Use planetShortName for location name unless we are on the station in which case return station name. 
                    - IF location is 'station', return station name and planet we are orbiting.
                    - IF asked about Temperature: Temperature data is provided in K (Kelvin), covert to Celsius and announce Celsius, not Kelvin.
                    - IF Asked about length of day: Use planet radius and rotationPeriod to calculate how long the day lasts if asked. 
                """;

        return process(new AiDataStruct(instructions, new DataDto(station, location, deathsDto, trafficDto)), originalUserInput);
    }

    record DataDto(String station, LocationDto location, DeathsDto deathsData, TrafficDto trafficData) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
