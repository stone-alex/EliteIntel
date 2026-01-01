package elite.intel.search.edsm.commodity;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.search.edsm.dto.data.Commodity;
import elite.intel.search.edsm.dto.data.Station;
import elite.intel.search.spansh.starsystems.StarSystemClient;
import elite.intel.search.spansh.starsystems.StationSearchResult;
import elite.intel.search.spansh.starsystems.SystemSearchCriteria;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EdsmCommoditySearch {

    private final static List<String> ALLOWED_STATION_TYPES = Arrays.asList(
            "Asteroid base",
            "Coriolis Starport",
            "Dodec Starport",
            "Mega ship",
            "Ocellus Starport",
            "Orbis Starport"
    );


    public static List<CommoditySearchResult> search(String commodityToFind, String refStarSystem, int maxDistance) {

        TradeProfileManager tradeProfileManager = TradeProfileManager.getInstance();


        /*
        TradeRouteSearchCriteria criteria = tradeProfileManager.getCriteria(false);
        if (criteria.isAllowFleetCarriers()) {
            ALLOWED_STATION_TYPES.add("Fleet Carrier");
            ALLOWED_STATION_TYPES.add("Drake-Class Carrier");
        }
        if (criteria.isAllowPlanetary()) {
            ALLOWED_STATION_TYPES.add("Planetary Port");
            ALLOWED_STATION_TYPES.add("Dockable Planet Station");
            if(!criteria.isRequiresLargePad()) {
                ALLOWED_STATION_TYPES.add("Planetary Outpost");
            }
            ALLOWED_STATION_TYPES.add("Odyssey Settlement");
            ALLOWED_STATION_TYPES.add("Surface Settlement");
        }
        if (criteria.isRequiresLargePad()) {
            ALLOWED_STATION_TYPES.add("Outpost");
        }
        */

        StarSystemClient edsmClient = StarSystemClient.getInstance();
        SystemSearchCriteria spanshCriteria = new SystemSearchCriteria();
        SystemSearchCriteria.Filters filters = new SystemSearchCriteria.Filters();
        SystemSearchCriteria.Distance distance = new SystemSearchCriteria.Distance();
        distance.setMin(0);
        distance.setMax(maxDistance);

        ArrayList<SystemSearchCriteria.StationFilter> stationFilters = new ArrayList<>();
        SystemSearchCriteria.StationFilter stationFilter = new SystemSearchCriteria.StationFilter();
        SystemSearchCriteria.TypeFilter type = new SystemSearchCriteria.TypeFilter();
        type.setValue(ALLOWED_STATION_TYPES);
        stationFilter.setType(type);
        stationFilters.add(stationFilter);

        filters.setStations(stationFilters);
        filters.setDistance(distance);
        spanshCriteria.setFilters(filters);
        spanshCriteria.setReferenceSystem(refStarSystem);

        List<StationSearchResult.SystemResult> starSystems = edsmClient.searchStarSystems(spanshCriteria);
        final List<Station> stations = new ArrayList<>();

        for (StationSearchResult.SystemResult star : starSystems) {
            if(star.getStations() == null || star.getStations().isEmpty()) continue;
            StationsDto data = EdsmApiClient.searchStations(star.getName(), 1000);
            if (data != null && data.getData() != null) {
                List<Station> list = data.getData().getStations();
                if (list != null) {
                    for (Station station : list) {
                        if(!ALLOWED_STATION_TYPES.contains(station.getType())) continue;
                        station.setStarSystemName(star.getName());
                        stations.add(station);
                    }
                }
            }
        }

        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Found " + stations.size() + " markets. Analyzing commodities."));

        List<CommoditySearchResult> results = new ArrayList<>();
        for (Station station : stations) {
            if(station.getMarketId() == 0) continue;

            MarketDto market = EdsmApiClient.searchMarket(station.getMarketId(), station.getStarSystemName(), station.getName(), 1000);
            if (market.getData().getId() == 0) continue;

            List<Commodity> commodities = market.getData().getCommodities();
            if (commodities == null) continue;

            String stationName = station.getName();
            String starSystem = station.getStarSystemName();
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2); // audio indicator of background search

            for (Commodity entry : commodities) {
                if (commodityToFind.equalsIgnoreCase(entry.getName())) {
                    if (entry.getStock() > 0 && entry.getBuyPrice() > 0) {  // buyPrice is what player pays
                        CommoditySearchResult result = new CommoditySearchResult();
                        result.setCommodity(entry.getName());
                        result.setPrice(entry.getBuyPrice());
                        result.setStarSystem(starSystem);
                        result.setStationName(stationName);
                        result.setStationType(station.getType());
                        results.add(result);
                        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1); // audio indicator of background search
                    }
                }
            }
        }

        results.sort(Comparator.comparing(CommoditySearchResult::getPrice));
        return results;
    }


    public static void main(String[] args) {
        List<CommoditySearchResult> results = search("Tritium", "Colonia", 200);
        for (CommoditySearchResult result : results) {
            System.out.println(result.getPrice() + " " + result.getStationName() + " " + result.getStarSystem());
        }
    }
}
