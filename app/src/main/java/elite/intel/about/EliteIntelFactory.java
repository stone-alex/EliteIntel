package elite.intel.about;

import elite.intel.about.dto.AICapabilitiesDto;
import elite.intel.gameapi.gamestate.events.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class EliteIntelFactory extends BaseJsonDto implements ToJsonConvertible {

    private static final EliteIntelFactory instance = new EliteIntelFactory();

    private EliteIntelFactory() {
        // Prevent instantiation.
    }

    public static EliteIntelFactory getInstance() {
        return instance;
    }

    public AICapabilitiesDto getCapabilities() {
        return new AICapabilitiesDto();
    }

}
