package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.FactionStats;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

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

