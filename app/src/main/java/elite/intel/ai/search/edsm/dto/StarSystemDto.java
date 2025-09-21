package elite.intel.ai.search.edsm.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.edsm.dto.data.StarSystemData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class StarSystemDto implements ToJsonConvertible {
    @SerializedName("data")
    public StarSystemData data;
    @SerializedName("timestamp")
    public long timestamp;

    public StarSystemData getData() {
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

