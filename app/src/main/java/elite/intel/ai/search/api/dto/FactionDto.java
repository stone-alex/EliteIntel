package elite.intel.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.api.dto.data.FactionStats;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class FactionDto implements ToJsonConvertible {
    @SerializedName("data")
    public FactionStats data;
    @SerializedName("timestamp")
    public long timestamp;

    public FactionStats getData() {
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

