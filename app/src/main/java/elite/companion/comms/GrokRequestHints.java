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
            "mining target"
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
