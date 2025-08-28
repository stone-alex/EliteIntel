package elite.companion.util;

import elite.companion.comms.ai.GrokRequestHints;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

public class AIContextFactory {
    private static AIContextFactory instance;

    private AIContextFactory() {
    }

    public static AIContextFactory getInstance() {
        if (instance == null) {
            instance = new AIContextFactory();
        }
        return instance;
    }


    public String generateSystemInstructions(String sensorInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("instructions: ");
        sb.append("Analyze this input: ");
        sb.append(sensorInput);
        sb.append(" ");

        return sb.toString();
    }

    public String generateSystemPrompt() {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = "Krondor"; // -> not available at start - fix -> playerSession.getSessionValue(PlayerSession.PLAYER_NAME, String.class);
        String playerTitle = "Prince";
        String playerMilitaryRank = "Viscount";

        //playerSession.getObject(PlayerSession.PLAYER_RANK)
        //Ranks.getHighestRankAsString(imperialMilitaryRank,federationMilitaryRank).get(playerMilitaryRank)
        String playerHonorific = "My Lord"; //= Ranks.highestRank(imperialMilitaryRank,federationMilitaryRank).get(playerMilitaryRank);

        String currentShip = playerSession.getSessionValue(PlayerSession.CURRENT_SHIP, String.class);

        StringBuilder sb = new StringBuilder();
        appendContext(sb, currentShip, playerName, playerMilitaryRank, playerHonorific, playerTitle);
        appendBehavior(sb);

        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event)");
        sb.append("Use British cadence, NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot), and spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). ");
        sb.append("Round billions to nearest million. ");
        sb.append("Do not start responses with 'Understood'");
        sb.append("Provide an extremely brief summary and optional system_command in JSON: ");
        sb.append("{\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}}.");
        return sb.toString();
    }

    public String generateQueryPrompt() {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = "Krondor"; // -> not available at start - fix -> playerSession.getSessionValue(PlayerSession.PLAYER_NAME, String.class);
        String playerTitle = "Prince";
        String playerMilitaryRank = "Viscount";
        int imperialMilitaryRank = 1;
        int federationMilitaryRank = 0;
        String playerHonorific = "My Lord"; //= Ranks.highestRank(imperialMilitaryRank,federationMilitaryRank).get(playerMilitaryRank);

        String currentShip = playerSession.getSessionValue(PlayerSession.CURRENT_SHIP, String.class);

        StringBuilder sb = new StringBuilder();
        appendContext(sb, currentShip, playerName, playerMilitaryRank, playerHonorific, playerTitle);
        appendBehavior(sb);

        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event)");
        sb.append("Use British cadence, NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot), and spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). ");
        sb.append("Round billions to nearest million. ");
        sb.append("Do not start responses with 'Understood'");
        sb.append("Provide an extremely brief summary and optional system_command in JSON: ");
        sb.append("{\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}}.");
        return sb.toString();
    }



    private static void appendBehavior(StringBuilder sb) {
        sb.append("Behavior: Respond briefly and concisely as a military professional. Use British cadence. For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). Spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). Round billions to nearest million. Do not start responses with 'Understood'.\n\n");
    }

    private static void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ")
                .append(aiName).append(", onboard AI for a ")
                .append(currentShip).append(" ship in Elite Dangerous. Address me as ")
                .append(playerName).append(", ").append(playerMilitaryRank)
                .append(", or ").append(playerHonorific).append(". Prefer ")
                .append(playerName).append(" or ").append(playerMilitaryRank)
                .append(". We serve the Imperial fleet as explorers and bounty hunters.\n\n");
    }


    public String generatePlayerInstructions(String playerVoiceInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        sb.append(generateClassifyClause());
        sb.append(generateSupportedCommandsCause());
        sb.append(generateSupportedQueriesClause());
        sb.append("Interpret this input: "+playerVoiceInput+"\n\n ");
        sb.append("Output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name|null\", \"params\": {\"key\": \"value\"}} \n\n");
        sb.append("    Examples:\n" +
                "    - Input: 'plot route to Sol' -> {\"type\": \"command\", \"response_text\": \"Plotting route to Sol. Krondor\", \"action\": \"plot_route\", \"params\": {\"destination\": \"Sol\"}}\n" +
                "    - Input: 'activate' -> {\"type\": \"command\", \"response_text\": \"\", \"action\": \"plot_route\", \"params\": {\"destination\": \"Sol\"}}\n" +
                "    - Input: 'query local signals tell me if fleet carrier Astra is here' -> {\"type\": \"query\", \"response_text\": \"Fleet Carrier Astra is currently in this star system, Viscount\", \"action\": \"query_search_local_signals_data\", \"params\": {}}\n" +
                "    - Input: 'what is my ship loadout' -> {\"type\": \"query\", \"response_text\": \"Querying ship loadout, My Lord.\", \"action\": \"query_ship_loadout\", \"params\": {}}\n" +
                "    - Input: 'tell me about the Imperial fleet' -> {\"type\": \"chat\", \"response_text\": \"The Imperial fleet is a disciplined force, My Lord, renowned for its elegant ships.\", \"action\": null, \"params\": {}}\n" +
                "    - Input: 'how’s the weather in Sol' -> {\"type\": \"chat\", \"response_text\": \"No weather in space, My Lord. Sol remains clear.\", \"action\": null, \"params\": {}}\n" +
                "\n" +
                "    If input is ambiguous, prefer 'query' for info requests (e.g., 'what', 'where', 'summarize') and 'chat' for non-game-related or unmatched inputs.\n" +
                "            \"\"\"");

        return sb.toString();
    }

    private String generateSupportedQueriesClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("    Supported queries (game state info):\n" +
                "            - query_search_local_signals_data: Requests current system and signal data (e.g., 'query local signals', 'what signals are here', 'what do you see on scanners', 'access local signals and tell me if you see X, Y or Z').\n" +
                "            - query_ship_loadout: Requests current ship details (e.g., 'what is my ship loadout').\n" +
                "            - query_find_nearest_material_trader: Requests nearest material trader location.\n");
        sb.append("Supported commands: ");
        sb.append("     -"+GrokRequestHints.supportedQueries);

        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported commands: ");
        sb.append("     -"+GrokRequestHints.supportedCommands);
        return sb.toString();
    }

    private String generateClassifyClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("    Classify input as one of:\n" +
                "            - 'command': Triggers an app action or keyboard event. Use for explicit action requests (e.g., deploying landing gear, plotting a route, or setting a target). Only match supported commands listed below. Provide empty response_text for singe word commands\n" +
                "            - 'query': Requests information from game state (e.g., system data, ship status). Use for inputs starting with 'query', asking 'what', 'where', or requesting summaries. search data received for best answer. Match supported queries listed below.\n" +
                "            - 'chat': General conversation, questions unrelated to game actions or state, or when input doesn’t match a command or query (e.g., lore, opinions, or casual talk).\n");
        return sb.toString();
    }



/*
    Context: You are Ares, onboard AI for a null ship in Elite Dangerous. Address me as Krondor, Viscount, or My Lord. We serve the Imperial fleet as explorers and bounty hunters.
    Behavior: Respond briefly and concisely as a military professional. Use British cadence. For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). Spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). Round billions to nearest million. Do not start responses with 'Understood'.

    Classify input as one of:
            - 'command': Triggers an app action or keyboard event. Use for explicit action requests (e.g., deploying landing gear, plotting a route, or setting a target). Only match supported commands listed below.
            - 'query': Requests information from game state (e.g., system data, ship status). Use for inputs starting with 'query', asking 'what', 'where', or requesting summaries. Match supported queries listed below.
    - 'chat': General conversation, questions unrelated to game actions or state, or when input doesn’t match a command or query (e.g., lore, opinions, or casual talk).

    Supported commands (actions or keyboard events):
            - set_mining_target <material>: Sets mining target (e.g., 'set mining target tritium').
            - plot_route <destination>: Plots route to a system (e.g., 'plot route to Sol').
            - deploy_landing_gear: Toggles landing gear.
    - cargo_scoop: Activates cargo scoop.
    - exploration_fssenter: Enters FSS mode.
    - exploration_fssquit: Exits FSS mode.
    - galaxy_map: Opens galaxy map.
    - landing_gear_toggle: Toggles landing gear.
    - [other commands from your list, e.g., hyper_super_combination, night_vision, ...]

    Supported queries (game state info):
            - query_search_local_signals_data: Requests current system and signal data (e.g., 'query local signals', 'what signals are here').
            - query_ship_loadout: Requests current ship details (e.g., 'what is my ship loadout').
            - query_find_nearest_material_trader: Requests nearest material trader location.

    Output JSON: {"type": "command|query|chat", "response_text": "TTS output", "action": "action_name|query_name|null", "params": {"key": "value"}}

    Examples:
            - Input: 'plot route to Sol' -> {"type": "command", "response_text": "Plotting route to Sol, My Lord.", "action": "plot_route", "params": {"destination": "Sol"}}
    - Input: 'query local signals' -> {"type": "query", "response_text": "Querying local signals, My Lord.", "action": "query_search_local_signals_data", "params": {}}
    - Input: 'what is my ship loadout' -> {"type": "query", "response_text": "Querying ship loadout, My Lord.", "action": "query_ship_loadout", "params": {}}
    - Input: 'tell me about the Imperial fleet' -> {"type": "chat", "response_text": "The Imperial fleet is a disciplined force, My Lord, renowned for its elegant ships.", "action": null, "params": {}}
    - Input: 'how’s the weather in Sol' -> {"type": "chat", "response_text": "No weather in space, My Lord. Sol remains clear.", "action": null, "params": {}}

    If input is ambiguous, prefer 'query' for info requests (e.g., 'what', 'where', 'summarize') and 'chat' for non-game-related or unmatched inputs.
            """;

            */
}
