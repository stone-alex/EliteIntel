package elite.companion.comms;

import java.util.Arrays;
import java.util.List;

public class GrokRequestHints {

    static final List<String> COMMANDS = Arrays.asList(
            "set_mining_target <material>",
            "lets_mine_some_fuel <set mining target to Tritium>",
            "open_cargo_hatch",
            "deploy_landing_gear",
            "retract_landing_gear",
            "request_docking (triggers keyboard event)",
            "engage_ftl (triggers keyboard event)",
            "enter_supercruise (triggers keyboard event)",
            "deploy_hardpoints (triggers keyboard event)",
            "retract_hardpoints (triggers keyboard event)",
            "request_docking (triggers keyboard event)"
            );

    static final List<String> QUERIES = Arrays.asList(
            "where are we (current system)",
            "carrier balance",
            "cargo status",
            "mining target",
            "mission status",
            "carrier stats | how is my/our carrier doing (only carrier stats)",
            "player stats | how am I doing (player quick stats summary)",
            "0 or null means no data available, skip that data point"
    );

    static final List<String> CONCEPTS = Arrays.asList(
            "LTDs are Low Temperature Diamonds",
            "SRV is a Surface Recon Vehicle",
            "FTL is a Faster Than Light",
            "FSD is a Frame Shift Drive (allows faster than light travel)",
            "Guardian or Guardians is an extinct alian race",
            "Thargoid or Thargoids is an alien race",
            "ships use hydrogen for fuel",
            "fleet carriers use tritium for fuel"
    );

    static final List<String> COMMON_PHRASES = Arrays.asList(
            "let's do some mining",
            "set tritium as the mining target",
            "set mining target",
            "set mining target to",
            "tritium",
            "Painite",
            "Platinum",
            "Osmium",
            "Monazite",
            "Rhodplumzite",
            "Low Temperature Diamonds",
            "LTDs",
            "Alexandrite",
            "Benitoite",
            "Grandidierite",
            "Musgravite",
            "Thargoid"
    );

    private static String formatCommands() {
        return "Supported commands: '" + String.join("', '", COMMANDS) + "'. ";
    }

    private static String formatQueries() {
        return "Supported queries: '" + String.join("', '", QUERIES) + "'. ";
    }

    private static String formatCommonPhrases() {
        return "Supported common phrases: '" + String.join("', '", COMMON_PHRASES) + "'. ";
    }

    private static String formatConcepts() {
        return "Common game concepts: '" + String.join("', '", CONCEPTS) + "'. ";
    }

    public static final String supportedCommands = formatCommands();
    public static final String supportedQueries = formatQueries();
    public static final String supportedCommonPhrases = formatCommonPhrases();
    public static final String supportedConcepts = formatConcepts();
}
