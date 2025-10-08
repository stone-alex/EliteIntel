package elite.intel.util.json;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;


public class PrimitiveData extends BaseJsonDto implements ToJsonConvertible {

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}

