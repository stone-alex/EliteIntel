package elite.intel.help;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.help.dto.AICapabilitiesDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class EliteIntelFactory extends BaseJsonDto implements ToJsonConvertible {

    private static final EliteIntelFactory instance = new EliteIntelFactory();
    private final AiCommandsAndQueries commandsAndQueries = AiCommandsAndQueries.getInstance();
    private EliteIntelFactory() {
        // Prevent instantiation.
    }

    public static EliteIntelFactory getInstance() {
        return instance;
    }

    public AICapabilitiesDto getCapabilities() {
        String description = "Your capabilities include the following ship controls, and data queries";
        return new AICapabilitiesDto(
                new DataDto(commandsAndQueries.getCommandMap(), commandsAndQueries.getQueries()),
                description
        );
    }

    public record DataDto(String commands, String queries) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}
