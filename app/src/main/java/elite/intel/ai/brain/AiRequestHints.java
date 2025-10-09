package elite.intel.ai.brain;

import elite.intel.ai.brain.handlers.commands.custom.Commands;
import elite.intel.ai.brain.handlers.query.QueryActions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The GrokRequestHints class is responsible for managing and formatting supported commands
 * and queries for interacting with specific systems or performing predefined actions.
 * It provides lists of these commands and queries, along with their formatting for display
 * or usage purposes.
 * <p>
 * This class is primarily designed to support functionality related to commands and queries
 * which are derived from enums or predefined constants. It also includes common phrases that
 * may be relevant in the context of the application's operation.
 * <p>
 * Key responsibilities:
 * - Maintain and initialize a list of supported commands by combining predefined enums and
 * user-specific extensions.
 * - Maintain and initialize a list of supported queries based on predefined queries.
 * - Provide formatted strings presenting the list of commands or queries.
 */
public class AiRequestHints {
    public static final List<String> COMMANDS;
    public static final List<String> QUERIES;

    static {
        List<String> commands = Arrays.stream(Commands.values())
                .map(Commands::getCommandWithPlaceholder)
                .collect(Collectors.toList());
        COMMANDS = commands;
    }

    static {
        QUERIES = Arrays.stream(QueryActions.values())
                .map(QueryActions::getAction)
                .collect(Collectors.toList());
    }

    private static String formatCommands() {
        return "Supported commands: '" + String.join("', '", COMMANDS) + "', ";
    }

    private static String formatQueries() {
        return "Supported queries: '" + String.join("', '", QUERIES) + "'. ";
    }

    private static String formatCustomCommands() {
        return "Supported custom commands: '" + String.join("', '", Commands.getCustomCommands()) + "'. ";
    }

    public static final String supportedQueries = formatQueries();
    public static final String customCommands = formatCustomCommands();
}