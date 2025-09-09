package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.DeathsData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

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

