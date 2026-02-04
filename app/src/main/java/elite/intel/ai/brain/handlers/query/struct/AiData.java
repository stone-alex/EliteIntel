package elite.intel.ai.brain.handlers.query.struct;


import elite.intel.util.yaml.ToYamlConvertable;

public interface AiData extends ToYamlConvertable {
    String getInstructions();
    ToYamlConvertable getData();
}
