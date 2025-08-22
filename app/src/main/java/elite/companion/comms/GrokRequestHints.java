package elite.companion.comms;

import java.util.Arrays;
import java.util.List;

public class GrokRequestHints {

    private static final List<String> COMMANDS = Arrays.asList(
            "set_mining_target <material>",
            "open_cargo_hatch",
            "deploy_landing_gear",
            "retract_landing_gear",
            "request_docking (triggers keyboard event)"
    );

    private static final List<String> QUERIES = Arrays.asList(
            "where are we (current system)",
            "carrier balance",
            "cargo status",
            "mining target",
            "mission status",
            "carrier stats | how is my/our carrier doing (only carrier stats)",
            "player stats | how am I doing (player quick stats summary)",
            "0 or null means no data available, skip that data point"
    );

    private static String formatCommands() {
        return "Supported commands: '" + String.join("', '", COMMANDS) + "'. ";
    }

    private static String formatQueries() {
        return "Supported queries: '" + String.join("', '", QUERIES) + "'. ";
    }

    public static final String supportedCommands = formatCommands();
    public static final String supportedQueries = formatQueries();
}
