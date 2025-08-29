package elite.companion.util;

import elite.companion.comms.ai.GrokRequestHints;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import java.util.Objects;

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
        appendBehavior(sb);
        sb.append("instructions: ");
        sb.append("Analyze this input: ");
        sb.append(sensorInput);
        sb.append(" ");

        return sb.toString();
    }

    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event)");
        sb.append("Use NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot), and spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        appendBehavior(sb);
        sb.append("Round billions to nearest million. ");
        sb.append("Provide an extremely brief summary and optional system_command in JSON: ");
        sb.append("{\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}}.");
        return sb.toString();
    }

    private static void getSessionValues(StringBuilder sb) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = String.valueOf(playerSession.get(PlayerSession.PLAYER_NAME));
        String playerTitle = String.valueOf(playerSession.get(PlayerSession.PLAYER_TITLE));
        String playerMilitaryRank = String.valueOf(playerSession.get(PlayerSession.PLAYER_RANK));
        String playerHonorific = Ranks.getPlayerHonorific();
        String currentShip = String.valueOf(playerSession.get(PlayerSession.CURRENT_SHIP));
        String missionStatement = String.valueOf(playerSession.get(PlayerSession.PLAYER_MISSION_STATEMENT));

        appendContext(sb,
                Objects.equals(currentShip, "null") ? "ship" : currentShip,
                Objects.equals(playerName, "null") ? "Commander" : playerName,
                Objects.equals(playerMilitaryRank, "null") ? "Commander" : playerMilitaryRank,
                Objects.equals(playerHonorific, "null") ? "Commander" : playerHonorific,
                Objects.equals(playerTitle, "null") ? "Commander" : playerTitle,
                Objects.equals(missionStatement, "null") ? "" : missionStatement
        );
        appendBehavior(sb);
        appendBehavior(sb);
    }

    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();

        getSessionValues(sb);

        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event)");
        appendBehavior(sb);
        sb.append("Provide an extremely brief summary and optional system_command in JSON: ");
        sb.append("{\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}}.");
        return sb.toString();
    }


    public static void appendBehavior(StringBuilder sb) {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();
        sb.append("Behavior:");
        sb.append(aiPersonality.getBehaviorClause());
        sb.append(aiCadence.getCadenceClause());
        sb.append("For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). Spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). Round billions to nearest million..\n\n");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
    }

    private static void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ")
                .append(aiName).append(", onboard AI for a ")
                .append(currentShip).append(" ship in Elite Dangerous. Address me as ")
                .append(playerName).append(", ")
                .append(playerMilitaryRank).append(", ")
                .append(playerTitle).append(", ")
                .append(" or ").append(playerHonorific)
                .append(". Prefer ")
                .append(playerName).append(" or ").append(playerMilitaryRank).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(missionStatement);
        }
        sb.append("\n\n");
    }


    public String generatePlayerInstructions(String playerVoiceInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        sb.append(generateClassifyClause());
        sb.append(generateSupportedCommandsCause());
        sb.append(generateSupportedQueriesClause());
        appendBehavior(sb);
        sb.append("Interpret this input: " + playerVoiceInput + "\n\n ");
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
        appendBehavior(sb);
        sb.append("Supported commands: ");
        sb.append("     -" + GrokRequestHints.supportedQueries);

        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported commands: ");
        sb.append("     -" + GrokRequestHints.supportedCommands);
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
}
