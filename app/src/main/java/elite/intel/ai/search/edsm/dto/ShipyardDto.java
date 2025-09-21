package elite.intel.ai.search.edsm.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.edsm.dto.data.ShipyardData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class ShipyardDto implements ToJsonConvertible {
    @SerializedName("data")
    public ShipyardData data;
    @SerializedName("timestamp")
    public long timestamp;

    public ShipyardData getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

