package elite.intel.ai.brain.handlers.query.struct;

import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AiDataStruct implements AiData {

    private final String instructions;
    private final ToYamlConvertable data;

    public AiDataStruct(String instructions, ToYamlConvertable data) {
        this.instructions = instructions;
        this.data = data;
    }

    @Override public String getInstructions() {
        return instructions;
    }

    @Override public ToYamlConvertable getData() {
        return data;
    }

    @Override public String toYaml() {
        return YamlFactory.toYaml(this);
    }
}
