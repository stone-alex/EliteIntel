package elite.intel.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.api.dto.data.SystemBodiesData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class SystemBodiesDto implements ToJsonConvertible {
    @SerializedName("data")
    public SystemBodiesData data;
    @SerializedName("timestamp")
    public long timestamp;

    public SystemBodiesData getData() {
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

