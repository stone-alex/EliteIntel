package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.ShipyardData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class ShipyardDto implements ToJsonConvertible {
    @SerializedName("data")
    public ShipyardData data;
    @SerializedName("timestamp")
    public long timestamp;

    public ShipyardData getData() {
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

