package elite.intel.search.edsm.monetize;

import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.MarketDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.search.edsm.dto.data.Commodity;
import elite.intel.search.edsm.dto.data.Station;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

public class MonetizeRoute {

    private final static List<String> ALLOWED_STATION_TYPES = Arrays.asList("asteroid base", "coriolis starport", "ocellus starport", "orbis starport");

    public static TradeTransaction findTrade(List<NavRouteDto> stops) {

        TradeProfileManager tradeProfileManager = TradeProfileManager.getInstance();
        TradeRouteSearchCriteria criteria = tradeProfileManager.getCriteria(false);

        if (criteria.isAllowFleetCarriers()) {
            ALLOWED_STATION_TYPES.add("fleet carrier");
        }
        if (criteria.isAllowPlanetary()) {
            ALLOWED_STATION_TYPES.add("planetary port");
            ALLOWED_STATION_TYPES.add("odyssey settlement");
        }
        if (!criteria.isRequiresLargePad()) {
            ALLOWED_STATION_TYPES.remove("outpost");
        }

        NavRouteDto destination = stops.getLast();
        List<Station> destinations = toStationWithMarket(destination.getName());
        List<Station> sources = new ArrayList<>();
        for (NavRouteDto stop : stops) {
            sources.addAll(toStationWithMarket(stop.getName()));
        }
        return MonetizeRoute.findTrade(sources, destinations);
    }


    private static List<Station> toStationWithMarket(String starSystemName) {
        StationsDto stationsData = EdsmApiClient.searchStations(starSystemName);
        if (stationsData == null || stationsData.getData() == null || stationsData.getData().getStations() == null) return new ArrayList<>();

        List<Station> stations = stationsData.getData().getStations();
        for (Station station : stations) {
            if(station.getType() == null) continue;
            if (!ALLOWED_STATION_TYPES.contains(station.getType().toLowerCase())) continue;
            station.setStarSystemName(starSystemName);
            MarketDto market = EdsmApiClient.searchMarket(station.getMarketId(), station.getName(), null);
            station.setCommodities(market.getData().getCommodities());
        }

        return stations;
    }


    /**
     * Finds the best trade route between source and destination trade stations.
     * The method identifies the commodity with the maximum profit by comparing
     * the minimum buy price from source stations and maximum sell price from
     * destination stations while considering supply and demand constraints.
     *
     * @param sourceStations the list of trade stations the player can buy commodities from,
     *                       each containing market information including buy price and supply.
     * @param destStations   the list of trade stations the player can sell commodities to,
     *                       each containing market information including sell price and demand.
     * @return a RouteTuple representing the best trade route with maximum profit, or null
     * if no profitable trade route is available.
     */
    private static TradeTransaction findTrade(List<Station> sourceStations, List<Station> destStations) {
        // Source: min buyPrice (player buys from station) with supply > 0
        Map<String, BuyInfo> sourceMinBuy = new HashMap<>();
        for (Station station : sourceStations) {
            String stationName = station.getName();
            String starSystem = station.getStarSystemName();
            String stationType = station.getType();
            List<Commodity> market = station.getCommodities();
            if (market == null) continue;
            for (Commodity entry : market) {
                if (entry.getStock() > 0 && entry.getBuyPrice() > 0) {  // buyPrice is what player pays
                    String comm = entry.getName();
                    int price = entry.getBuyPrice();
                    BuyInfo current = sourceMinBuy.get(comm);
                    if (current == null || price < current.buyPrice) {
                        sourceMinBuy.put(comm, new BuyInfo(comm, starSystem, stationName, stationType, price, entry.getStock()));
                    }
                }
            }
        }

        // Dest: max sellPrice (player sells to station) with demand > 0
        Map<String, SellInfo> destMaxSell = new HashMap<>();
        for (Station station : destStations) {
            String stationName = station.getName();
            String starSystem = station.getStarSystemName();
            String stationType = station.getType();
            List<Commodity> market = station.getCommodities();
            if (market == null) continue;
            for (Commodity entry : market) {
                if (entry.getDemand() > 1 && entry.getSellPrice() > 0) {  // sellPrice is what player gets
                    String comm = entry.getName();
                    int price = entry.getSellPrice();
                    SellInfo current = destMaxSell.get(comm);
                    if (current == null || price > current.sellPrice) {
                        destMaxSell.put(comm, new SellInfo(comm, starSystem, stationName, stationType, price, entry.getDemand()));
                    }
                }
            }
        }

        // Find best profit
        TradeTransaction best = null;
        int maxProfit = 0;
        for (String comm : sourceMinBuy.keySet()) {
            if (destMaxSell.containsKey(comm)) {
                BuyInfo buy = sourceMinBuy.get(comm);
                SellInfo sell = destMaxSell.get(comm);
                int profit = sell.sellPrice - buy.buyPrice;
                if (profit > maxProfit) {
                    maxProfit = profit;
                    best = new TradeTransaction(buy, sell);
                }
            }
        }

        return best;  // null if no profitable trades
    }

    public static class BuyInfo extends BaseJsonDto implements ToJsonConvertible {
        private String commodity;
        private String stationName;
        private String starSystem;
        private String stationType;
        private int buyPrice;
        private long supply;

        public BuyInfo(String commodity, String starSystem, String stationName, String stationType, int buyPrice, long supply) {
            this.commodity = commodity;
            this.starSystem = starSystem;
            this.stationName = stationName;
            this.stationType = stationType;
            this.buyPrice = buyPrice;
            this.supply = supply;
        }

        public String getCommodity() {
            return commodity;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public String getStationType() {
            return stationType;
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public long getSupply() {
            return supply;
        }
    }

    public static class SellInfo extends BaseJsonDto implements ToJsonConvertible {
        private String commodity;
        private String starSystem;
        private String stationName;
        private String stationType;
        private int sellPrice;
        private long demand;

        public SellInfo(String commodity, String starSystem, String stationName, String stationType, int sellPrice, long demand) {
            this.commodity = commodity;
            this.starSystem = starSystem;
            this.stationName = stationName;
            this.stationType = stationType;
            this.sellPrice = sellPrice;
            this.demand = demand;
        }

        public String getCommodity() {
            return commodity;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStationType() {
            return stationType;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public long getDemand() {
            return demand;
        }
    }

    public static class TradeTransaction {
        private BuyInfo source;
        private SellInfo destination;

        public TradeTransaction(BuyInfo source, SellInfo destination) {
            this.source = source;
            this.destination = destination;
        }

        public BuyInfo getSource() {
            return source;
        }

        public SellInfo getDestination() {
            return destination;
        }
    }
}
