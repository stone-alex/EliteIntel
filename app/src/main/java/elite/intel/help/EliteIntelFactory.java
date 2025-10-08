package elite.intel.help;

import elite.intel.help.dto.AICapabilitiesDto;
import elite.intel.ai.brain.AiRequestHints;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
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
        String supportedCommands = AiRequestHints.supportedCommands;
        String customCommands = AiRequestHints.customCommands;
        String supportedQueries = AiRequestHints.supportedQueries;
        return new AICapabilitiesDto(
                supportedCommands,
                customCommands,
                supportedQueries,
                "Your capabilities include the following ship controls: " + supportedCommands + ", custom commands "+customCommands+" the following queries: " + supportedQueries + ", or chat on any subject. help is available via 'help me with' command. There is also a detailed wiki on project GitHub"
        );
    }

}
