package elite.intel.search.edsm.utils;

import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.search.edsm.dto.data.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EdsmUtils {

    public final static List<String> ALLOWED_STATION_TYPES = Arrays.asList("asteroid base", "coriolis starport", "ocellus starport", "orbis starport");

    public static List<Station> toStationWithMarket(String starSystemName) {
        StationsDto stationsData = EdsmApiClient.searchStations(starSystemName, 0);
        if (stationsData == null || stationsData.getData() == null || stationsData.getData().getStations() == null) return new ArrayList<>();

        List<Station> stations = stationsData.getData().getStations();
        for (Station station : stations) {
            if(station.getType() == null) continue;
            if (!ALLOWED_STATION_TYPES.contains(station.getType().toLowerCase())) continue;
            station.setStarSystemName(starSystemName);
            MarketDto market = EdsmApiClient.searchMarket(station.getMarketId(), station.getName(), null, 1000);
            station.setCommodities(market.getData().getCommodities());
        }

        return stations;
    }
}
