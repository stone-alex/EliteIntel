package elite.intel.ai.search.spansh.station;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class DestinationDto implements ToJsonConvertible {

    private String json;

    public void setJson(String json) {
        this.json = json;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(json);
    }
}
