package elite.intel.ai.search.api.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.ai.search.api.dto.data.StarInRadius;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class StarsWithinRadiusDto implements ToJsonConvertible {
    @SerializedName("systems")
    public List<StarInRadius> systems;
    @SerializedName("timestamp")
    public long timestamp;

    public List<StarInRadius> getSystems() {
        return systems;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

