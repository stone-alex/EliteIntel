package elite.intel.search.edsm.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.search.edsm.dto.data.TrafficData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class TrafficDto implements ToJsonConvertible {
    @SerializedName("data")
    public TrafficData data;
    @SerializedName("timestamp")
    public long timestamp;

    public TrafficData getData() {
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

