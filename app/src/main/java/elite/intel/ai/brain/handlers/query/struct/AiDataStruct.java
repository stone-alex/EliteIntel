package elite.intel.ai.brain.handlers.query.struct;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AiDataStruct implements AiData {

    private String instructions;
    private ToJsonConvertible data;

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @Override public ToJsonConvertible getData() {
        return data;
    }

    public void setData(ToJsonConvertible data) {
        this.data = data;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
