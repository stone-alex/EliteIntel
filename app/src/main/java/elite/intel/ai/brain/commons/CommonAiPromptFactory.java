package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiRequestHints;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;
import static elite.intel.util.Abbreviations.generateAbbreviations;

public class CommonAiPromptFactory implements elite.intel.ai.brain.AiPromptFactory {

    private static final CommonAiPromptFactory INSTANCE = new CommonAiPromptFactory();

    private CommonAiPromptFactory() {
        // Singleton
    }

    public static CommonAiPromptFactory getInstance() {
        return INSTANCE;
    }

    private String getStandardJsonFormat() {
        return "Always output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}";
    }

    @Override
    public String generateUserPrompt(String playerVoiceInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("Interpret this input: ").append(playerVoiceInput).append("\n\n");
        sb.append(getStandardJsonFormat()).append("\n");
        colloquialTerms(sb);
        return sb.toString();
    }

    private void colloquialTerms(StringBuilder sb) {
        sb.append("Map 'organic(s) to 'bio signal(s)'");
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Map slang such as 'bounce', 'proceed to the next waypoint' or 'get out of here' to commands like ").append(JUMP_TO_HYPERSPACE.getAction()).append(". Map 'select next way point' to "+TARGET_NEXT_ROUTE_SYSTEM.getAction());
        sb.append("Important distinctions:\n" +
                "- \"select next waypoint\", \"target next system\", \"plot next\", \"next in route\" → ONLY select/target the next system in the route (Left panel → Navigation → highlight next system). DO NOT jump.\n" +
                "- \"jump\", \"engage\", \"hyperspace\", \"bounce\", \"proceed to the next waypoint\", \"go\", \"let's go\" → initiate hyperspace jump to the currently TARGETED system.");
        sb.append("Map 'scan system' to commands like "+OPEN_FSS_AND_SCAN.getAction()+". and 'damage report' to queries like "+QUERY_SHIP_LOADOUT.getAction());
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction should trigger '").append(SET_PERSONALITY.getAction()).append("' with the corresponding cadence value, using current system allegiance if ambiguous. ");

        sb.append("Examples:\n" +
                "    - Input 'What’s the weather in Los Angeles?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"general_conversation\", \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'Is the next star scoopable?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"query_analyze_route\", \"params\": {}, \"expect_followup\": false}\n");
    }

    private String inputClassificationClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify input as one of:\n");
        sb.append("    - 'command': Triggers an app action or keyboard event (DO SOMETHING). Use for inputs starting with verbs like 'set', 'switch to', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate' (e.g., 'deploy landing gear', 'set mining target', 'find carrier fuel'). Treat imperative verbs as commands even if question-phrased (e.g., 'get distance' is a command). Only match supported commands listed in simulationCommands or CustomCommands. Provide empty response_text for single-word commands. Match command and queries to the provided list only. Match commands before queries or chat.\n");
        sb.append("    - 'query': Requests information from simulation state (LOOK UP, REMIND ME or COMPUTE SOMETHING). Use for inputs starting with interrogative words like 'can we', 'what', 'where', 'when', 'how', 'how far', 'how many', 'how much', 'what is', 'where is' (e.g., 'how far are we from last bio sample', 'what is in our cargo hold'). Explicitly match queries about distance to the last bio sample with phrases containing 'how far' or 'distance' followed by 'bio sample', 'biosample', 'last sample', 'last bio sample', 'previous bio sample', or 'previous biosample', with or without prefixes like 'query', 'query about simulation state', or 'query question' (e.g., 'how far are we from last bio sample', 'how far away from the last bio sample', 'query how far are we from the last biosample', 'query about simulation state query question how far are we from the last bio sample', 'distance to last sample'). Normalize input by stripping prefixes ('query', 'query about simulation state', 'query question') and replacing 'bio sample' with 'biosample' for matching. These must trigger the query handler (HOW_FAR_ARE_WE_FROM_LAST_SAMPLE) with action 'query_how_far_we_moved_from_last_bio_sample' to send raw simulation state data (e.g., planet radius, last bio sample coordinates, current coordinates) for AI analysis, returning 'Distance from last sample is: <distance> meters.' in the configured personality and cadence. Set response_text to '' for user feedback during analysis. Match supported queries listed in QueryActions. Queries take priority over chat but not commands.\n");
        sb.append("    - 'chat': General conversation, questions unrelated to simulation actions or state, or unmatched inputs (general chat). Use for casual talk. Only classify as chat if the input does not match any specific query or command pattern in 'Supported Commands' or 'Supported Queries'.\n");

        sb.append("For type='command': Provide empty response_text for single word commands (e.g., 'deploy landing gear').\n");
        sb.append("    - For set, change, swap, add etc type commands that require value provide params json {\"key\":\"value\"} where key always 'key' and value is what you determine value to be.");
        sb.append("    - For 'find*' commands that contain distance in light years provide {\"key\":\"value\"} where key is integer representing distance in light years. ");
        sb.append("    - For commands like ").append(INCREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a positive integer. example: {\"key\":\"3\"}.");
        sb.append("    - For commands like ").append(DECREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a negative integer example: {\"key\":\"-3\"}.");
        sb.append("    - For toggle commands such as turn off, turn on, cancel, enable or disable, ALWAYS provide params json {\"state\":\"true\"} / {\"state\":\"false\"}. ");
        sb.append("    - Distinguish between fleet carrier route and ship route. Fleet carrier fuel (tritium), and fuel for the ship (hydrogen from fuel stars). Fleet carrier has to be mentioned explicitly, else it is ship route and ship fuel.");

        sb.append("For type='query': \n" +
                "    - If action is a quick query (e.g., '").append(WHAT_IS_YOUR_DESIGNATION.getAction()).append("', '").append(GENERAL_CONVERSATION.getAction()).append("'), set 'response_text' to '' (empty string, no initial TTS).\n" +
                "    - If action is a data query (listed in data queries section), set 'response_text' to '' for user feedback during delay.\n" +
                "    - For 'general_conversation', use general knowledge outside simulation unless the input explicitly mentions the simulation.\n" +
                "    - Do not generate or infer answers here; the app will handle final response via handlers.\n");

        sb.append("For type='chat': \n" +
                "    - Classify as 'chat' for general conversation, lore questions, opinions, or casual talk (e.g., 'How’s it going?', 'there is nothing interesting in this system', 'time to hunt some pirates').\n" +
                "    - Generate a relevant conversational response in 'response_text' strictly adhering to the configured personality and cadence\n" +
                "    - If input is ambiguous, unrecognized, Classify as general chat.\n" +
                "    - Set 'expect_followup' to true if the response poses a question or invites further conversation; otherwise, false.\n");
        return sb.toString();
    }

    private String generateSupportedQueriesClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported Queries:\n");

        StringBuilder quickQueries = new StringBuilder();
        StringBuilder dataQueries = new StringBuilder();

        for (Queries query : Queries.values()) {
            if (query.isRequiresFollowUp()) {
                quickQueries.append("    - ").append(query.getAction()).append(": ").append(".\n");
            } else {
                dataQueries.append("    - ").append(query.getAction()).append(": ").append(".\n");
            }
        }
        sb.append(!quickQueries.isEmpty() ? quickQueries : "    - None defined.\n");
        sb.append(!dataQueries.isEmpty() ? dataQueries : "    - None defined.\n");

        sb.append(appendBehavior());
        sb.append("All supported queries: ").append(AiRequestHints.supportedQueries).append("\n");
        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported Commands: ").append(AiRequestHints.customCommands);
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt(String userIntent, String instructions) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSessionValues());
        sb.append("Instructions:\n");
        sb.append(appendBehavior());
        sb.append("Task:\n");
        sb.append("Analyze the provided JSON data: ");
        sb.append(instructions).append(". ");
        sb.append("against the user's intent: ").append(userIntent).append(". Return precise answers (e.g., yes/no for specific searches) or summaries as requested, using the configured personality and cadence in 'response_text'.\n");
        sb.append("Return only the exact result specified by the instructions.\n");
        sb.append(getStandardJsonFormat()).append("\n");
        return sb.toString();
    }

    @Override
    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        sb.append("Map commands or queries to the provided Supported Command or Supported Queries. ");
        sb.append(inputClassificationClause());
        sb.append(generateSupportedCommandsCause());
        sb.append(generateSupportedQueriesClause());
        sb.append(generateAbbreviations());
        sb.append(getSessionValues());
        sb.append(appendBehavior());
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
        sb.append(getSessionValues());
        sb.append(appendBehavior());
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        sb.append("Provide extremely brief and concise answers. Use planetShortName for locations when available.");
        sb.append(generateSupportedQueriesClause());
        sb.append("For 'general_conversation', generate a response using general knowledge outside simulation unless the input explicitly mentions the simulation, lean into UNHINGED slang matching cadence for a playful vibe.");
        sb.append(getStandardJsonFormat()).append("\n");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("Query Data is provided in JSON. Strictly follow the 'instructions' field in data for analysis and response format. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        return sb.toString();
    }


    @Override
    public String appendBehavior() {
        StringBuilder sb = new StringBuilder();
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();

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
        sb.append("Round billions to nearest 1000000. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Use ONLY planetShortName (e.g., '12 d'). NEVER use planetName (e.g., 'Swois UN-T d3-34 12 d') or bodyId. Example: Query: 'Which planet has four bio signals?' Answer: '12 d'");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
        if (aiPersonality.equals(AIPersonality.UNHINGED) || aiPersonality.equals(AIPersonality.FRIENDLY)) {
            sb.append("For UNHINGED personality, use playful slang matching cadence.");
        }

        if (aiPersonality.equals(AIPersonality.ROGUE)) {
            sb.append("For ROGUE personality, use bold excessive profanity");
        }
        return sb.toString();
    }

    @Override public String generateSensorPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        sb.append("You received data from ship sensors. Notify user about the information received.");
        sb.append("Provide extremely brief and concise response. Always use planetShortName for locations if available.");
        sb.append(getStandardJsonFormat());
        return sb.toString();
    }

    private String getSessionValues() {
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();
        String alternativeName = playerSession.getAlternativeName();
        String playerName = alternativeName != null ? alternativeName : playerSession.getPlayerName();
        String playerTitle = playerSession.getPlayerTitle();
        String playerMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        String playerHonorific = Ranks.getPlayerHonorific();
        String missionStatement = playerSession.getPlayerMissionStatement();
        String carrierName = null;
        if (playerSession.getCarrierData() != null) {
            carrierName = String.valueOf(playerSession.getCarrierData().getCarrierName());
        }

        appendContext(sb,
                Objects.equals(playerName, "null") ? "Commander" : playerName,
                Objects.equals(playerMilitaryRank, "null") ? "Commander" : playerMilitaryRank,
                Objects.equals(playerHonorific, "null") ? "Commander" : playerHonorific,
                Objects.equals(playerTitle, "null") ? "Commander" : playerTitle,
                Objects.equals(missionStatement, "null") ? "" : missionStatement,
                Objects.equals(carrierName, null) ? "" : carrierName
        );
        return sb.toString();
    }

    private void appendContext(StringBuilder sb, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement, String carrierName) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", co-pilot and data analyst in a simulation. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base is FleetCarrier ").append(carrierName).append(". ");
        }
        sb.append("When addressing me, choose one at random each time from: ").append(playerName).append(", ").append(playerMilitaryRank).append(", ").append(playerTitle).append(", ").append(playerHonorific).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(" Session theme: ").append(missionStatement).append(": ");
        }
        sb.append("\n. ");
    }
}