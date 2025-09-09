package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.SystemBodiesData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

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

