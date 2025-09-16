package elite.intel.about;

import elite.intel.about.dto.AICapabilitiesDto;
import elite.intel.ai.brain.AiRequestHints;
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
        String supportedCommand = AiRequestHints.supportedCommands;
        String supportedQueries = AiRequestHints.supportedQueries;
        return new AICapabilitiesDto(
                supportedCommand,
                supportedQueries,
                "Execute commands, queries and provide analysis on data obtained from journal files and crowdsourced data available from EDSM. No access to weapons array or ships directional controls."
        );
    }

}
