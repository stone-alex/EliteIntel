package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.TrafficData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

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

