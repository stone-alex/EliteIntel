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

    private static final String FORMAT_PREFIX =
            "Respond in plain spoken English only. No lists, no bullets, no numbering, no markdown. " +
                    "Two to four sentences maximum unless the question explicitly asks for a full list.\n\n";

    @Override public String getInstructions() {
        return FORMAT_PREFIX + instructions;
    }

    @Override public ToYamlConvertable getData() {
        return data;
    }

    @Override public String toYaml() {
        return YamlFactory.toYaml(this);
    }
}
