package elite.intel.ai.brain;

import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommands;
import elite.intel.ai.brain.handlers.query.QueryActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(AiRequestHints.class);
    public static final List<String> COMMANDS;
    public static final List<String> QUERIES;
    public static final List<String> COMMON_PHRASES = Arrays.asList(
            "tritium", "mining", "material trader", "plot route", "supercruise", "landing gear", "cargo scoop",
            "request docking"
    );

    static {
        List<String> commands = Arrays.stream(CustomCommands.values())
                .map(CustomCommands::getCommandWithPlaceholder)
                .collect(Collectors.toList());

        // Add user-friendly commands and queries
        commands.addAll(Arrays.asList(GameCommands.getUserCommands()));

        // Load game bindings
        commands.addAll(Arrays.asList(GameCommands.getUserCommands()));
        COMMANDS = commands;
    }

    static {
        List<String> queries = Arrays.stream(QueryActions.values())
                .map(QueryActions::getAction)
                .collect(Collectors.toList());


        QUERIES = queries;
    }

    private static String formatCommands() {
        return "Supported commands: '" + String.join("', '", COMMANDS) + "'. ";
    }

    private static String formatQueries() {
        return "Supported queries: '" + String.join("', '", QUERIES) + "'. ";
    }

    public static final String supportedCommands = formatCommands();

    public static final String supportedQueries = formatQueries();
}