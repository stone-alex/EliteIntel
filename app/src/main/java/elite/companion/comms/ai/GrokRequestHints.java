package elite.companion.comms.ai;

import elite.companion.comms.handlers.command.CommandActionsCustom;
import elite.companion.comms.handlers.command.CommandActionsGame;
import elite.companion.comms.handlers.query.QueryActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GrokRequestHints {
    private static final Logger log = LoggerFactory.getLogger(GrokRequestHints.class);
    public static final List<String> COMMANDS;
    public static final List<String> QUERIES;
    public static final List<String> COMMON_PHRASES = Arrays.asList(
            "tritium", "mining", "material trader", "plot route", "supercruise", "landing gear", "cargo scoop",
            "request docking"
    );

    static {
        List<String> commands = Arrays.stream(CommandActionsCustom.values())
                .map(CommandActionsCustom::getCommandWithPlaceholder)
                .collect(Collectors.toList());

        // Add user-friendly commands and queries
        commands.addAll(Arrays.asList(CommandActionsGame.getUserCommands()));

        // Load game bindings
        commands.addAll(Arrays.asList(CommandActionsGame.getUserCommands()));
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