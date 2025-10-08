package elite.intel.gameapi.gamestate.dtos;

import elite.intel.util.json.GsonFactory;

public class BaseJsonDto {

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
