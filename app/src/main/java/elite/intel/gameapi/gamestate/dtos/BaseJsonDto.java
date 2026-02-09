package elite.intel.gameapi.gamestate.dtos;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class BaseJsonDto implements ToJsonConvertible, ToYamlConvertable {

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public String toYaml(){
        return YamlFactory.toYaml(this);
    }
}
