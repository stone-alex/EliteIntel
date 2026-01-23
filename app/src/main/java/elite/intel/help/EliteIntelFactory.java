package elite.intel.help;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.help.dto.AICapabilitiesDto;
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
        String customCommands = AiCommandsAndQueries.commands;
        String supportedQueries = AiCommandsAndQueries.queries;
        String description = "Your capabilities include the following ship controls: commands " + customCommands + " the following queries: " + supportedQueries + ", or chat on any subject. help is available via 'help me with' command. There is also a detailed wiki on project GitHub";
        return new AICapabilitiesDto(
                customCommands,
                supportedQueries,
                description
        );
    }

}
