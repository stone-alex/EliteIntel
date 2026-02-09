package elite.intel.search.edsm.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.search.edsm.dto.data.DeathsData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class DeathsDto implements ToJsonConvertible, ToYamlConvertable {
    @SerializedName("data")
    public DeathsData data;

    public DeathsData getData() {
        return data;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override public String toYaml() {
        return YamlFactory.toYaml(this);
    }
}

