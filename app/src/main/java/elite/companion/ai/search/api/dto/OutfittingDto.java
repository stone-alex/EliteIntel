package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.OutfittingData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class OutfittingDto implements ToJsonConvertible {
    @SerializedName("data")
    public OutfittingData data;
    @SerializedName("timestamp")
    public long timestamp;

    public OutfittingData getData() {
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

