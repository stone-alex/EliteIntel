package elite.intel.ai.brain.handlers.query.struct;

import elite.intel.util.json.ToJsonConvertible;

public interface AiData extends ToJsonConvertible {
    String getInstructions();
    ToJsonConvertible getData();
}
