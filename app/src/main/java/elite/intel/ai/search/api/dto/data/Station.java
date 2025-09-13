package elite.intel.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Station {
    @SerializedName("id")
    public int id;
    @SerializedName("marketId")
    public long marketId;
    @SerializedName("type")
    public String type;
    @SerializedName("name")
    public String name;
    @SerializedName("body")
    public StationBody body;
    @SerializedName("distanceToArrival")
    public double distanceToArrival;
    @SerializedName("allegiance")
    public String allegiance;
    @SerializedName("government")
    public String government;
    @SerializedName("economy")
    public String economy;
    @SerializedName("secondEconomy")
    public String secondEconomy;
    @SerializedName("haveMarket")
    public boolean haveMarket;
    @SerializedName("haveShipyard")
    public boolean haveShipyard;
    @SerializedName("haveOutfitting")
    public boolean haveOutfitting;
    @SerializedName("otherServices")
    public List<String> otherServices;
    @SerializedName("controllingFaction")
    public ControllingFaction controllingFaction;
    @SerializedName("updateTime")
    public StationUpdateTime updateTime;

    public int getId() {
        return id;
    }

    public long getMarketId() {
        return marketId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public StationBody getBody() {
        return body;
    }

    public double getDistanceToArrival() {
        return distanceToArrival;
    }

    public String getAllegiance() {
        return allegiance;
    }

    public String getGovernment() {
        return government;
    }

    public String getEconomy() {
        return economy;
    }

    public String getSecondEconomy() {
        return secondEconomy;
    }

    public boolean isHaveMarket() {
        return haveMarket;
    }

    public boolean isHaveShipyard() {
        return haveShipyard;
    }

    public boolean isHaveOutfitting() {
        return haveOutfitting;
    }

    public List<String> getOtherServices() {
        return otherServices;
    }

    public ControllingFaction getControllingFaction() {
        return controllingFaction;
    }

    public StationUpdateTime getUpdateTime() {
        return updateTime;
    }
}
