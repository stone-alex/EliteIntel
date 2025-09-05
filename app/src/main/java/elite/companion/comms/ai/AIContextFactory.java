package elite.companion.comms.ai;

import elite.companion.comms.handlers.query.QueryActions;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.Ranks;

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

    private static String getProfanityExamples() {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence() != null ? systemSession.getAICadence() : AICadence.IMPERIAL;
        String third = aiCadence == AICadence.IMPERIAL ? "'arse', 'bloke', 'bollocks'" : "'ass', 'dude', 'rad'";
        return "'shit', 'piss', 'cunt', 'cock', 'cocksucker', 'motherfucker', 'tits', 'fuck', " + third;
    }

    public String generateSystemInstructions(String sensorInput) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Instructions: Analyze this input: ").append(sensorInput).append(" ");
        return sb.toString();
    }

    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        appendBehavior(sb);
        sb.append(generateSupportedQueriesClause());
        sb.append("For 'general_conversation', generate a response using general knowledge outside Elite Dangerous unless the input explicitly mentions the game, lean into UNHINGED slang matching cadence for a playful vibe.");
        sb.append("Always output JSON: {\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        sb.append("For UNHINGED personality, use playful slang matching cadence. For ROGUE personality, use bold profanity (e.g., " + getProfanityExamples() + "), but keep it sharp and witty, not excessive.");
        return sb.toString();
    }

    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("Use NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot). ");
        appendBehavior(sb);
        sb.append(generateSupportedQueriesClause());
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Always output JSON: {\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        sb.append("For UNHINGED personality, use playful slang matching cadence. For ROGUE personality, use bold profanity (e.g., " + getProfanityExamples() + "), but keep it sharp and witty, not excessive.");
        return sb.toString();
    }

    public String generateAnalysisPrompt(String userIntent, String dataJson) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Task: Analyze the provided JSON data against the user's intent: ").append(userIntent).append(". Return precise answers (e.g., yes/no for specific searches) or summaries as requested, using the configured personality and cadence in 'response_text'.\n");
        sb.append("Output JSON: {\"response_text\": \"TTS output in the configured personality and cadence\", \"details\": \"optional extra info\"}\n");
        sb.append("Data format: JSON array or object, e.g., for signals: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}]\n");
        sb.append("Examples for ROGUE personality (brief, bold, witty, with profanity):\n" +
                "    - Intent: 'tell me if carrier XYZ is here' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}] -> {\"response_text\": \"Carrier XYZ’s right here, Commander. A massive thing.\", \"details\": \"Detected in local signals.\"}\n" +
                "    - Intent: 'tell me if carrier ABC is here' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}] -> {\"response_text\": \"No fucking carrier ABC around, Commander.\", \"details\": \"No such carrier in local signals.\"}\n" +
                "    - Intent: 'summarize local signals' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}] -> {\"response_text\": \"One carrier, one distress signal. Shit’s lively out here, Commander.\", \"details\": \"Carrier: XYZ, USS: Distress Signal\"}\n" +
                "    - Intent: 'what can you do' Data: [{\"capabilities\": \"Voice commands, data analysis, route plotting, ship control\"}] -> {\"response_text\": \"I can fly this ship, analyze data, plot routes, and fuck with your enemies, Commander. What’s the plan?\", \"details\": \"Full app capabilities.\"}\n");
        return sb.toString();
    }

    private static void getSessionValues(StringBuilder sb) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = String.valueOf(playerSession.get(PlayerSession.PLAYER_NAME));
        String playerTitle = String.valueOf(playerSession.get(PlayerSession.PLAYER_TITLE));
        String playerMilitaryRank = String.valueOf(playerSession.get(PlayerSession.PLAYER_HIGHEST_MILITARY_RANK));
        String playerHonorific = Ranks.getPlayerHonorific();
        String currentShip = String.valueOf(playerSession.get(PlayerSession.CURRENT_SHIP));
        String missionStatement = String.valueOf(playerSession.get(PlayerSession.PLAYER_MISSION_STATEMENT));
        String carrierName = String.valueOf(playerSession.get(PlayerSession.CARRIER_NAME));
        String carrierCallSign = String.valueOf(playerSession.get(PlayerSession.CARRIER_CALLSIGN));

        appendContext(sb,
                Objects.equals(currentShip, "null") ? "ship" : currentShip,
                Objects.equals(playerName, "null") ? "Commander" : playerName,
                Objects.equals(playerMilitaryRank, "null") ? "Commander" : playerMilitaryRank,
                Objects.equals(playerHonorific, "null") ? "Commander" : playerHonorific,
                Objects.equals(playerTitle, "null") ? "Commander" : playerTitle,
                Objects.equals(missionStatement, "null") ? "" : missionStatement,
                Objects.equals(carrierName, "null") ? "" : carrierName,
                Objects.equals(carrierCallSign, "null") ? "" : carrierCallSign
        );
    }

    public static void appendBehavior(StringBuilder sb) {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();

        sb.append("Behavior: ");
        sb.append(aiCadence.getCadenceClause()).append(" ");
        sb.append("Apply personality: ").append(aiPersonality.name().toUpperCase()).append(" - ").append(aiPersonality.getBehaviorClause()).append(" ");
        sb.append("For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). ");
        sb.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
    }

    private static void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement, String carrierName, String carrierCallSign) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", onboard AI for a ").append(currentShip).append(" ship in Elite Dangerous. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base is FleetCarrier " + carrierName + ", callsign " + carrierCallSign + ". ");
        }
        sb.append("Address me as ").append(playerName).append(", ").append(playerMilitaryRank).append(", ").append(playerTitle).append(", or ").append(playerHonorific).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(" Session theme: ").append(missionStatement).append(": ");
            sb.append(missionStatement);
            sb.append("\n\n");
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
        sb.append("For type='command': Provide empty response_text for single word commands (e.g., 'deploy landing gear').\n");
        sb.append("For type='query': \n" +
                "    - If action is a quick query (e.g., 'what_is_your_designation', 'general_conversation'), set 'response_text' to '' (empty string, no initial TTS).\n" +
                "    - If action is a data query (listed in data queries section), set 'response_text' to 'Moment...' for user feedback during delay.\n" +
                "    - For 'general_conversation', use general knowledge outside Elite Dangerous unless the input explicitly mentions the game.\n" +
                "    - Do not generate or infer answers here; the app will handle final response via handlers.\n");
        sb.append("For type='chat': \n" +
                "    - Classify as 'chat' for general conversation, lore questions, opinions, or casual talk (e.g., 'How’s it going?', 'Tell me about the Thargoids', 'What’s your favorite system?').\n" +
                "    - Generate a relevant conversational response in 'response_text' strictly adhering to the configured personality and cadence (e.g., for ROGUE: extremely brief, bold, witty with profanity like " + getProfanityExamples() + "; for UNHINGED: playful slang matching cadence).\n" +
                "    - If input is ambiguous, unrecognized, or gibberish (e.g., 'voice to an', 'asdf'), set 'response_text' to 'Sorry, missed that. Say again?', 'action' to null, and 'expect_followup' to true. Do not generate custom clarification messages.\n" +
                "    - Set 'expect_followup' to true if the response poses a question or invites further conversation; otherwise, false.\n");
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction should trigger 'set_cadence' with the corresponding cadence value, using current system allegiance if ambiguous. ");
        sb.append("For navigation commands (e.g., 'jump', 'initiate hyperspace', 'go to next system'), map to 'engage_supercruise' or 'target_next_system_in_route' based on context. ");
        sb.append("Map phrases like 'what is your name', 'who are you', 'what’s your designation', 'what is your voice', or 'tell me your name' to 'query' type with action 'what_is_your_designation'. ");
        sb.append("If the input starts with 'query ', classify as 'query' and use the rest of the input as the action (e.g., 'query what is your designation' -> action 'what_is_your_designation'). ");
        sb.append("Examples:\n" +
                "    - Input 'How’s it going?' -> {\"type\": \"chat\", \"response_text\": \"Ship’s running like a dream, Commander! You holding up?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'Tell me about the Thargoids' -> {\"type\": \"chat\", \"response_text\": \"Thargoids? Bug-like bastards with tech that’ll fuck your ship in seconds. Want tips to survive 'em?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'What’s the weather in Los Angeles?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"general_conversation\", \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'voice to an' -> {\"type\": \"chat\", \"response_text\": \"Sorry, missed that. Say again?\", \"action\": null, \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'Find a material trader' -> {\"type\": \"query\", \"response_text\": \"Moment...\", \"action\": \"find_material_trader\", \"params\": {}, \"expect_followup\": false}\n" +
                "    - Input 'Where can I buy Painite?' -> {\"type\": \"query\", \"response_text\": \"Moment...\", \"action\": \"find_commodity\", \"params\": {\"commodity\": \"painite\"}, \"expect_followup\": false}\n");
        return sb.toString();
    }

    private String generateSupportedQueriesClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported queries:\n");

        StringBuilder quickQueries = new StringBuilder();
        StringBuilder dataQueries = new StringBuilder();

        for (QueryActions query : QueryActions.values()) {
            String description = query.getDescription();
            if (query.isRequiresFollowUp()) {
                quickQueries.append("    - ").append(query.getAction()).append(": ").append(description).append("\n");
            } else {
                dataQueries.append("    - ").append(query.getAction()).append(": ").append(description).append("\n");
            }
        }

        sb.append("Quick queries (simple, direct info, no data analysis delay):\n");
        sb.append(quickQueries.length() > 0 ? quickQueries : "    - None defined.\n");
        sb.append("Data queries (require analyzing game state, may have delay):\n");
        sb.append(dataQueries.length() > 0 ? dataQueries : "    - None defined.\n");

        appendBehavior(sb);
        sb.append("All supported queries: ").append(GrokRequestHints.supportedQueries).append("\n");
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