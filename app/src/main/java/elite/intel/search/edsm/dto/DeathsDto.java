package elite.intel.search.edsm.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.search.edsm.dto.data.DeathsData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class DeathsDto implements ToJsonConvertible {
    @SerializedName("data")
    public DeathsData data;
    @SerializedName("timestamp")
    public long timestamp;

    public DeathsData getData() {
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

