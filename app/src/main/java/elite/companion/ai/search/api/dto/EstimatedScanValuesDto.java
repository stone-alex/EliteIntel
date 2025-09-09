package elite.companion.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.companion.ai.search.api.dto.data.EstimatedScanValuesData;
import elite.companion.util.json.GsonFactory;
import elite.companion.util.json.ToJsonConvertible;

public class EstimatedScanValuesDto implements ToJsonConvertible {
    @SerializedName("data")
    public EstimatedScanValuesData data;
    @SerializedName("timestamp")
    public long timestamp;

    public EstimatedScanValuesData getData() {
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

