package elite.intel.search.spansh.traderoute;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class TradeRouteResponse implements ToJsonConvertible {

    @SerializedName("result") public List<RouteLeg> result;
    @SerializedName("state") public String state;
    @SerializedName("status") public String status;

    public List<RouteLeg> getResult() {
        return result;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

// ──────────────────────────────────────────────────────────────────────

class RouteLeg implements ToJsonConvertible {
    @SerializedName("commodities") public List<CommodityLeg> commodities;
    @SerializedName("cumulative_profit") public long cumulativeProfit;
    @SerializedName("destination") public StationInfo destination;
    @SerializedName("distance") public double distance;
    @SerializedName("source") public StationInfo source;
    @SerializedName("total_profit") public long totalProfit;

    public List<CommodityLeg> getCommodities() {
        return commodities;
    }

    public long getCumulativeProfit() {
        return cumulativeProfit;
    }

    public StationInfo getDestination() {
        return destination;
    }

    public double getDistance() {
        return distance;
    }

    public StationInfo getSource() {
        return source;
    }

    public long getTotalProfit() {
        return totalProfit;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

// ──────────────────────────────────────────────────────────────────────

class CommodityLeg implements ToJsonConvertible {
    @SerializedName("amount") public int amount;
    @SerializedName("destination_commodity") public CommodityInfo destinationCommodity;
    @SerializedName("name") public String name;
    @SerializedName("profit") public long profit;
    @SerializedName("source_commodity") public CommodityInfo sourceCommodity;
    @SerializedName("total_profit") public long totalProfit;

    public int getAmount() {
        return amount;
    }

    public CommodityInfo getDestinationCommodity() {
        return destinationCommodity;
    }

    public String getName() {
        return name;
    }

    public long getProfit() {
        return profit;
    }

    public CommodityInfo getSourceCommodity() {
        return sourceCommodity;
    }

    public long getTotalProfit() {
        return totalProfit;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

// ──────────────────────────────────────────────────────────────────────

class CommodityInfo implements ToJsonConvertible {
    @SerializedName("buy_price") public long buyPrice;
    @SerializedName("demand") public long demand;
    @SerializedName("sell_price") public long sellPrice;
    @SerializedName("supply") public long supply;

    public long getBuyPrice() {
        return buyPrice;
    }

    public long getDemand() {
        return demand;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public long getSupply() {
        return supply;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

// ──────────────────────────────────────────────────────────────────────

class StationInfo implements ToJsonConvertible {
    @SerializedName("distance_to_arrival") public double distanceToArrival;
    @SerializedName("market_id") public long marketId;
    @SerializedName("market_updated_at") public long marketUpdatedAt;
    @SerializedName("station") public String station;
    @SerializedName("system") public String system;
    @SerializedName("system_id64") public long systemId64;   // we ignore it but keep for completeness
    @SerializedName("x") public double x;
    @SerializedName("y") public double y;
    @SerializedName("z") public double z;

    public double getDistanceToArrival() {
        return distanceToArrival;
    }

    public long getMarketId() {
        return marketId;
    }

    public long getMarketUpdatedAt() {
        return marketUpdatedAt;
    }

    public String getStation() {
        return station;
    }

    public String getSystem() {
        return system;
    }

    public long getSystemId64() {
        return systemId64;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}