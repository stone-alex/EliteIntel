package elite.intel.ai.brain.handlers.query.struct;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AiDataStruct implements AiData {

    private String instructions;
    private ToJsonConvertible data;

    public AiDataStruct(String instructions, ToJsonConvertible data) {
        this.instructions = instructions;
        this.data = data;
    }

    @Override public String getInstructions() {
        return instructions;
    }

    @Override public ToJsonConvertible getData() {
        return data;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
