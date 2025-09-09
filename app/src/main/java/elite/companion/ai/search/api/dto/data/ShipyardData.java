package elite.companion.ai.search.api.dto.data;

import com.google.gson.annotations.SerializedName;

public class ShipyardData {
    @SerializedName("id")
    public int id;
    @SerializedName("id64")
    public long id64;
    @SerializedName("name")
    public String name;
    @SerializedName("marketId")
    public long marketId;
    @SerializedName("sId")
    public int sId;
    @SerializedName("sName")
    public String sName;
    @SerializedName("ships")
    public Object ships; // Null in example; can be refined to List if structure known

    public int getId() {
        return id;
    }

    public long getId64() {
        return id64;
    }

    public String getName() {
        return name;
    }

    public long getMarketId() {
        return marketId;
    }

    public int getsId() {
        return sId;
    }

    public String getsName() {
        return sName;
    }

    public Object getShips() {
        return ships;
    }
}
