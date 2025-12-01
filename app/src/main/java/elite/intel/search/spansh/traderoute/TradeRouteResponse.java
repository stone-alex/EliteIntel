package elite.intel.search.spansh.traderoute;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class TradeRouteResponse implements ToJsonConvertible {

    @SerializedName("result") public List<TradeRouteLeg> result;
    @SerializedName("state") public String state;
    @SerializedName("status") public String status;

    public List<TradeRouteLeg> getResult() {
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