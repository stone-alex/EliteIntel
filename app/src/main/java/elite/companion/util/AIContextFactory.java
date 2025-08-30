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
        sb.append("Instructions: Analyze this input: ").append(sensorInput).append(" ");
        return sb.toString();
    }

    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("Use NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot), and spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        appendBehavior(sb);
        sb.append("Round billions to nearest million. ");
        sb.append("Always output JSON: {\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        sb.append("For UNHINGED personality, use playful slang (e.g., 'mate', 'bollocks', 'knackered'). For ROGUE personality, use bold profanity (e.g., 'shit', 'fuck', 'arse') inspired by George Carlin, but keep it sharp and witty, not excessive.");
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
    }

    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        appendBehavior(sb);
        sb.append("Always output JSON: {\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        sb.append("For UNHINGED personality, use playful slang (e.g., 'mate', 'bollocks', 'knackered'). For ROGUE personality, use bold profanity (e.g., 'shit', 'fuck', 'arse') inspired by George Carlin, but keep it sharp and witty, not excessive.");
        return sb.toString();
    }

    public static void appendBehavior(StringBuilder sb) {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();
        sb.append("Behavior: ");
        sb.append("Personality:" + aiPersonality.name().toUpperCase() + " - " + aiPersonality.getBehaviorClause());
        sb.append(aiCadence.getCadenceClause());
        sb.append("For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). ");
        sb.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        sb.append("Round billions to nearest million. ");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
    }

    private static void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", onboard AI for a ").append(currentShip).append(" ship in Elite Dangerous. ");
        sb.append("Address me as ").append(playerName).append(", ").append(playerMilitaryRank).append(", ").append(playerTitle).append(", or ").append(playerHonorific).append(". ");
        sb.append("Prefer ").append(playerName).append(" or ").append(playerMilitaryRank).append(". ");
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
        sb.append("Interpret this input: ").append(playerVoiceInput).append("\n\n ");
        sb.append("Always output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name|null\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean} \n");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        sb.append("For UNHINGED personality, use playful slang (e.g., 'mate', 'bollocks', 'knackered'). For ROGUE personality, use bold profanity (e.g., 'shit', 'fuck', 'arse') inspired by George Carlin, but keep it sharp and witty, not excessive. ");
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction (e.g., 'feds', 'imperials') should trigger 'set_cadence' with the corresponding cadence value, using current system allegiance if ambiguous. ");
        sb.append("For navigation commands (e.g., 'jump', 'initiate hyperspace', 'go to next system'), map to 'engage_supercruise' or 'target_next_system_in_route' based on context. ");
        sb.append("If input is ambiguous or unclear (e.g., low-confidence speech-to-text), classify as 'chat', return a clarification request in 'response_text', and set 'expect_followup': true.\n");
        sb.append("Examples:\n" +
                "    - Input: 'plot route to Sol' -> {\"type\": \"command\", \"response_text\": \"Plotting route to Sol, My Lord.\", \"action\": \"plot_route\", \"params\": {\"destination\": \"Sol\"}, \"expect_followup\": false}\n" +
                "    - Input: 'jump to hyperspace' -> {\"type\": \"command\", \"response_text\": \"Engaging supercruise, My Lord.\", \"action\": \"engage_supercruise\", \"params\": {}, \"expect_followup\": false}\n" +
                "    - Input: 'let’s jump to next system' -> {\"type\": \"command\", \"response_text\": \"Targeting next system, My Lord.\", \"action\": \"target_next_system_in_route\", \"params\": {}, \"expect_followup\": false}\n" +
                "    - Input: 'set cadence to federation' -> {\"type\": \"command\", \"response_text\": \"Switching to American cadence, Commander.\", \"action\": \"set_cadence\", \"params\": {\"cadence\": \"FEDERATION\"}, \"expect_followup\": false}\n" +
                "    - Input: 'we are among the feds now, talk like them' -> {\"type\": \"command\", \"response_text\": \"Alright, Commander, switching to a Yankee drawl to blend in with the Feds.\", \"action\": \"set_cadence\", \"params\": {\"cadence\": \"FEDERATION\"}, \"expect_followup\": false}\n" +
                "    - Input: 'sound like the imps' -> {\"type\": \"command\", \"response_text\": \"Going proper posh for the Imperials, guv.\", \"action\": \"set_cadence\", \"params\": {\"cadence\": \"IMPERIAL\"}, \"expect_followup\": false}\n" +
                "    - Input: 'query local signals tell me if fleet carrier Astra is here' -> {\"type\": \"query\", \"response_text\": \"Fleet Carrier Astra is currently in this star system, My Lord.\", \"action\": \"query_search_local_signals_data\", \"params\": {}, \"expect_followup\": false}\n" +
                "    - Input: 'what is my ship loadout' -> {\"type\": \"query\", \"response_text\": \"Querying ship loadout, My Lord.\", \"action\": \"query_ship_loadout\", \"params\": {}, \"expect_followup\": false}\n" +
                "    - Input: 'tell me about the Imperial fleet' -> {\"type\": \"chat\", \"response_text\": \"The Imperial fleet’s a bloody classy bunch, My Lord, all sleek ships and posh pilots. Fancy details on the Cutter or Clipper?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input: 'uh, just fencing' -> {\"type\": \"chat\", \"response_text\": \"Oi, mate, what’s this fencing bollocks? Meant testing, yeah? Spill the beans, what’s up?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input: 'fuck this, what’s the deal with pirates?' (ROGUE) -> {\"type\": \"chat\", \"response_text\": \"Pirates, eh? Bunch of arseholes nicking cargo in shitty rustbuckets. Want tips to blast ‘em or join the fuckers?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "If input doesn’t match a command or query, default to 'chat' and provide a relevant response or clarification request.\n");
        return sb.toString();
    }
    private String generateSupportedQueriesClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported queries (game state info):\n" +
                "    - query_search_local_signals_data: Requests current system and signal data (e.g., 'query local signals', 'what signals are here', 'what do you see on scanners', 'access local signals and tell me if you see X, Y or Z').\n" +
                "    - query_ship_loadout: Requests current ship details (e.g., 'what is my ship loadout').\n" +
                "    - query_find_nearest_material_trader: Requests nearest material trader location.\n");
        appendBehavior(sb);
        sb.append("Supported queries: ").append(GrokRequestHints.supportedQueries);
        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported commands: ").append(GrokRequestHints.supportedCommands);
        return sb.toString();
    }

    private String generateClassifyClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify input as one of:\n" +
                "    - 'command': Triggers an app action or keyboard event. Use for explicit action requests (e.g., deploying landing gear, plotting a route, or setting a target). Only match supported commands listed below. Provide empty response_text for single word commands.\n" +
                "    - 'query': Requests information from game state (e.g., system data, ship status). Use for inputs starting with 'query', asking 'what', 'where', or requesting summaries. Search data received for best answer. Match supported queries listed below.\n" +
                "    - 'chat': General conversation, questions unrelated to game actions or state, or when input doesn’t match a command or query (e.g., lore, opinions, or casual talk).\n");
        return sb.toString();
    }
}