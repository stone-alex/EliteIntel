package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.StationsData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class StationsDto implements ToJsonConvertible {
    @SerializedName("data")
    public StationsData data;
    @SerializedName("timestamp")
    public long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public StationsData getData() {
        return data;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

