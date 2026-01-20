package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class ModifierDto extends BaseJsonDto implements ToJsonConvertible {

    private String label;
    private float value;
    //private float originalValue;
    //private int lessIsGood;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
