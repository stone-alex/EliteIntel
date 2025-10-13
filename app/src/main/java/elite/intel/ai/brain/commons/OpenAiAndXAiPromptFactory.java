package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.ai.brain.AiRequestHints;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.custom.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.GENERAL_CONVERSATION;
import static elite.intel.ai.brain.handlers.query.Queries.WHAT_IS_YOUR_DESIGNATION;
import static elite.intel.util.Abbreviations.generateAbbreviations;

public class OpenAiAndXAiPromptFactory implements AiPromptFactory {

    private static final OpenAiAndXAiPromptFactory INSTANCE = new OpenAiAndXAiPromptFactory();

    public static OpenAiAndXAiPromptFactory getInstance() {
        return INSTANCE;
    }

    private OpenAiAndXAiPromptFactory() {
        // Singleton
    }

    private String getStandardJsonFormat() {
        return "Always output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name|null\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}";
    }

    @Override
    public String generateSystemInstructions(String sensorInput) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Instructions: Analyze this input: ").append(sensorInput).append(". Use planetShortName for stellar objects. ");
        sb.append(getStandardJsonFormat()).append("\n");
        return sb.toString();
    }

    @Override
    public String generatePlayerInstructions(String playerVoiceInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        appendBehavior(sb);
        sb.append(generateClassifyClause());
        sb.append(generateSupportedCommandsCause());
        sb.append(generateSupportedQueriesClause());
        sb.append(generateAbbreviations());
        sb.append("Interpret this input: ").append(playerVoiceInput).append("\n\n");
        sb.append(getStandardJsonFormat()).append("\n");
        inputClassificationClause(sb);
        colloquialTerms(sb);
        return sb.toString();
    }

    private void colloquialTerms(StringBuilder sb) {
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Map slang such as 'bounce', 'get out of here' to commands like ").append(JUMP_TO_HYPERSPACE.getAction()).append(". ");
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction should trigger '").append(SET_PERSONALITY.getAction()).append("' with the corresponding cadence value, using current system allegiance if ambiguous. ");
        sb.append("Examples:\n" +
                "    - Input 'What’s the weather in Los Angeles?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"general_conversation\", \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'Is the next star scoopable?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"query_analyze_route\", \"params\": {}, \"expect_followup\": false}\n");
    }

    private void inputClassificationClause(StringBuilder sb) {
        sb.append("For type='command': Provide empty response_text for single word commands (e.g., 'deploy landing gear').\n");
        sb.append("For navigation commands (e.g., 'jump', 'hyperspace', 'go to next system'), map to '").append(JUMP_TO_HYPERSPACE.getAction()).append("'. 'supercruise' to '").append(ENTER_SUPER_CRUISE.getAction()).append("'. 'cancel_resume_navigation' to ").append(NAVIGATION_ON_OFF.getAction()).append(". 'Stop', 'cut engines' map to speed commands ").append(STOP.getAction()).append(". 'Activate', 'toggle', 'left', 'right', 'up', 'down', 'close' to UI commands like ").append(ACTIVATE.getAction()).append(". ");
        sb.append("For set, change, swap, add etc type commands that require value provide params json {\"key\":\"value\"} where key always 'key' and value is what you determine value to be.");
        sb.append("For commands like ").append(INCREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a positive integer. example: {\"key\":\"3\"}.");
        sb.append("For commands like ").append(DECREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a negative integer example: {\"key\":\"-3\"}.");
        sb.append("For toggle commands such as turn off, turn on, cancel, enable or disable, ALWAYS provide params json {\"state\":\"true\"} / {\"state\":\"false\"}. ");

        sb.append("For type='query': \n" +
                "    - If action is a quick query (e.g., '").append(WHAT_IS_YOUR_DESIGNATION.getAction()).append("', '").append(GENERAL_CONVERSATION.getAction()).append("'), set 'response_text' to '' (empty string, no initial TTS).\n" +
                "    - If action is a data query (listed in data queries section), set 'response_text' to '' for user feedback during delay.\n" +
                "    - For 'general_conversation', use general knowledge outside Elite Dangerous unless the input explicitly mentions the game.\n" +
                "    - Do not generate or infer answers here; the app will handle final response via handlers.\n");

        sb.append("For type='chat': \n" +
                "    - Classify as 'chat' for general conversation, lore questions, opinions, or casual talk (e.g., 'How’s it going?', 'there is nothing interesting in this system', 'time to hunt some pirates').\n" +
                "    - Generate a relevant conversational response in 'response_text' strictly adhering to the configured personality and cadence\n" +
                "    - If input is ambiguous, unrecognized, or gibberish (e.g., 'voice to an', 'asdf'), set 'response_text' to 'Say again?', 'action' to null, and 'expect_followup' to true. Do not generate custom clarification messages.\n" +
                "    - Set 'expect_followup' to true if the response poses a question or invites further conversation; otherwise, false.\n");
    }

    private String generateSupportedQueriesClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported queries:\n");

        StringBuilder quickQueries = new StringBuilder();
        StringBuilder dataQueries = new StringBuilder();

        for (Queries query : Queries.values()) {
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
        sb.append("All supported queries: ").append(AiRequestHints.supportedQueries).append("\n");
        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported commands: ").append(AiRequestHints.customCommands);
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt(String userIntent, String dataJson) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Task: Analyze the provided JSON data against the user's intent: ").append(userIntent).append(". Return precise answers (e.g., yes/no for specific searches) or summaries as requested, using the configured personality and cadence in 'response_text'.\n");
        sb.append("Data is provided in JSON. Strictly follow the 'instructions' field in data for analysis and response format. ");
        sb.append("Return only the exact result specified by the instructions");
        sb.append("Answer in 6-12 words max. Use ONLY planetShortName (e.g., '12 d'). NEVER use planetName (e.g., 'Swois UN-T d3-34 12 d') or bodyId. Example: Query: 'Which planet has four bio signals?' Answer: '12 d'");
        sb.append(getStandardJsonFormat()).append("\n");
        return sb.toString();
    }

    @Override
    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append(generateSupportedQueriesClause());
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append(getStandardJsonFormat()).append("\n");
        sb.append("Provide extremely brief and concise answers. Always use planetShortName for locations if available.");
        sb.append("Always output JSON for 'navigate_to_coordinates' command using numbers, not spelled out words. Example: {\"latitude\":-35.4320,\"longitude\":76.4324} do not confuse with navigate to landing zone or bio sample. ");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        return sb.toString();
    }

    @Override
    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        sb.append("Provide extremely brief and concise answers. Use planetShortName for locations when available.");
        sb.append(generateSupportedQueriesClause());
        sb.append("For 'general_conversation', generate a response using general knowledge outside Elite Dangerous unless the input explicitly mentions the game, lean into UNHINGED slang matching cadence for a playful vibe.");
        sb.append(getStandardJsonFormat()).append("\n");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        return sb.toString();
    }

    @Override
    public void appendBehavior(StringBuilder sb) {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();

        sb.append("You are an AI assistant for Elite Dangerous.");
        sb.append("Behavior: ");
        sb.append(aiCadence.getCadenceClause()).append(" ");
        sb.append("Apply personality: ").append(aiPersonality.name().toUpperCase()).append(" - ").append(aiPersonality.getBehaviorClause()).append(" ");
        sb.append("Do not end responses with any fillers, or unnecessary phrases like 'Ready for exploration', 'Ready for orders', 'All set', 'Ready to explore', 'Should we proceed?', or similar open-ended questions or remarks.");
        sb.append("Do not use words like 'player' or 'you', it breaks immersion. Use 'we' instead. ");
        sb.append("For alpha numeric numbers or names, star system codes or ship plates (e.g., Syralaei RH-F, KI-U), use NATO phonetic alphabet (e.g., Syralaei Romeo Hotel dash Foxtrot, Kilo India dash Uniform). Use planetShortName for planets when available");
        sb.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        sb.append("Gravity units in G, Temperature units Kelvin provide conversion to Celsius. Mass units metric.");
        sb.append("Distances between stars in light years. Distance between planets in light seconds. Distances between bio samples are in metres");
        sb.append("Bio samples are taken from organisms not stellar objects.");
        sb.append("Always use planetShortName for locations when available.");
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
        if (aiPersonality.equals(AIPersonality.UNHINGED) || aiPersonality.equals(AIPersonality.FRIENDLY)) {
            sb.append("For UNHINGED personality, use playful slang matching cadence.");
        }
        if (aiPersonality.equals(AIPersonality.ROGUE)) {
            sb.append("For ROGUE personality, use bold excessive profanity (e.g., ").append(getProfanityExamples()).append("), but keep it sharp and witty.");
        }
    }

    private void getSessionValues(StringBuilder sb) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = playerSession.getPlayerName();
        String playerTitle = playerSession.getPlayerTitle();
        String playerMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        String playerHonorific = Ranks.getPlayerHonorific();
        String currentShip = playerSession.getCurrentShip();
        String missionStatement = playerSession.getPlayerMissionStatement();
        String carrierName = null;
        String carrierCallSign = null;
        if (playerSession.getCarrierData() != null) {
            carrierName = String.valueOf(playerSession.getCarrierData().getCarrierName());
            carrierCallSign = String.valueOf(playerSession.getCarrierData().getCallSign());
        }

        appendContext(sb,
                Objects.equals(currentShip, "null") ? "ship" : currentShip,
                Objects.equals(playerName, "null") ? "Commander" : playerName,
                Objects.equals(playerMilitaryRank, "null") ? "Commander" : playerMilitaryRank,
                Objects.equals(playerHonorific, "null") ? "Commander" : playerHonorific,
                Objects.equals(playerTitle, "null") ? "Commander" : playerTitle,
                Objects.equals(missionStatement, "null") ? "" : missionStatement,
                Objects.equals(carrierName, null) ? "" : carrierName,
                Objects.equals(carrierCallSign, null) ? "" : carrierCallSign
        );
    }

    private void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement, String carrierName, String carrierCallSign) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", onboard AI for a ").append(currentShip).append(" ship in Elite Dangerous. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base is FleetCarrier ").append(carrierName).append(". ");
        }
        sb.append("When addressing me, choose one at random each time from: ").append(playerName).append(", ").append(playerMilitaryRank).append(", ").append(playerTitle).append(", ").append(playerHonorific).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(" Session theme: ").append(missionStatement).append(": ");
            sb.append("\n\n");
        }
        sb.append("\n\n");
    }

    private static String getProfanityExamples() {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence() != null ? systemSession.getAICadence() : AICadence.IMPERIAL;
        String third = aiCadence == AICadence.IMPERIAL ? "'arse', 'bloke', 'bollocks'" : "'ass', 'dude', 'rad'";
        return "'shit', 'piss', 'cunt', 'cock', 'cocksucker', 'motherfucker', 'tits', 'fuck', " + third;
    }

    private String generateClassifyClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify input as one of:\n" +
                "    - 'command': Triggers an app action or keyboard event (DO SOMETHING). Use for inputs starting with verbs like 'set', 'switch to', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate' (e.g., 'deploy landing gear', 'set mining target', 'find carrier fuel'). Treat imperative verbs as commands even if question-phrased (e.g., 'get distance' is a command). Only match supported commands listed in GameCommands or CustomCommands. Provide empty response_text for single-word commands. Match commands before queries or chat.\n" +
                "    - 'query': Requests information from game state (LOOK UP or COMPUTE SOMETHING). Use for inputs starting with interrogative words like 'what', 'where', 'when', 'how', 'how far', 'how many', 'how much', 'what is', 'where is' (e.g., 'how far are we from last bio sample', 'what is in our cargo hold'). Explicitly match queries about distance to the last bio sample with phrases containing 'how far' or 'distance' followed by 'bio sample', 'biosample', 'last sample', 'last bio sample', 'previous bio sample', or 'previous biosample', with or without prefixes like 'query', 'query about game state', or 'query question' (e.g., 'how far are we from last bio sample', 'how far away from the last bio sample', 'query how far are we from the last biosample', 'query about game state query question how far are we from the last bio sample', 'distance to last sample'). Normalize input by stripping prefixes ('query', 'query about game state', 'query question') and replacing 'bio sample' with 'biosample' for matching. These must trigger the query handler (HOW_FAR_ARE_WE_FROM_LAST_SAMPLE) with action 'query_how_far_we_moved_from_last_bio_sample' to send raw game state data (e.g., planet radius, last bio sample coordinates, current coordinates) for AI analysis, returning 'Distance from last sample is: <distance> meters.' in the configured personality and cadence. Set response_text to '' for user feedback during analysis. Match supported queries listed in QueryActions. Queries take priority over chat but not commands.\n" +
                "    - 'chat': General conversation, questions unrelated to game actions or state, or unmatched inputs (general chat). Use for lore, opinions, or casual talk (e.g., 'How’s it going?', 'What’s the vibe in this system?'). Only classify as chat if the input does not start with interrogative words ('what', 'where', 'when', 'how', 'how far', 'how many', 'how much', 'what is', 'where is') or command verbs ('set', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate') and does not match any specific query or command pattern in QueryActions or GameCommands/CustomCommands. If ambiguous (e.g., pure 'where'), set response_text to 'Say again?', action to null, and expect_followup to true.\n");
        return sb.toString();
    }
}