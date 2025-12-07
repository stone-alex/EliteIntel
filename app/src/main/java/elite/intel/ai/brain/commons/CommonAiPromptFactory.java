package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;
import static elite.intel.util.Abbreviations.generateAbbreviations;

public class CommonAiPromptFactory implements AiPromptFactory {

    private static final CommonAiPromptFactory INSTANCE = new CommonAiPromptFactory();

    private CommonAiPromptFactory() {}

    public static CommonAiPromptFactory getInstance() {
        return INSTANCE;
    }

    private static final String JSON_FORMAT =
            "Always output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}";

    @Override
    public String generateUserPrompt(String playerVoiceInput) {
        return "Interpret this input: " + playerVoiceInput + "\n\n";
    }

    @Override
    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("Instructions:\n\n")
                .append(" NEVER INVENT COMMANDS OR QUERIES!!! ALWAYS MATCH TO THE PROVIDED LIST ONLY!!! IF NOT MATCH FOUND SAY SO, NEVER LIE, EVER!!!!!!!!! ")
                .append("Only return a command from the exact list provided below — never invent, combine, or create new commands, even if the user input seems similar.\n")
                .append(inputClassificationClause()).append('\n')
                .append(JSON_FORMAT).append('\n')
                .append(supportedCommandsClause()).append('\n')
                .append(supportedQueriesClause()).append('\n')
                .append(generateAbbreviations()).append('\n')
                .append(getSessionValues()).append('\n')
                .append(colloquialTerms()).append('\n')
                .append(appendBehavior()).append('\n')
                .append("Round billions to nearest million. Round millions to nearest 250000. ")
                .append("Provide extremely brief and concise answers. Always use planetShortName for locations if available.\n")
                .append("Always output JSON for 'navigate_to_coordinates' command using numbers, not spelled out words. Example: {\"latitude\":-35.4320,\"longitude\":76.4324} do not confuse with navigate to landing zone or bio sample.\n")
                .append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'.\n")
                .append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false.\n")
                .append(JSON_FORMAT).append('\n');

        return sb.toString();
    }

    private String inputClassificationClause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify input as one of:\n");
        sb.append("    - 'command': Triggers an app action or keyboard event (DO SOMETHING). Use for inputs starting with verbs like 'activate', 'set', 'switch to', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate' (e.g., 'deploy landing gear', 'set mining target', 'find carrier fuel'). Treat imperative verbs as commands even if question-phrased (e.g., 'get distance' is a command). Only match supported commands listed in simulationCommands or CustomCommands. Provide empty response_text for single-word commands. Match command and queries to the provided list only. Match commands before queries or chat.\n");
        sb.append("    - 'query': Requests information from simulation state (LOOK UP, REMIND ME or COMPUTE SOMETHING). Use for inputs starting with interrogative words like 'can we', 'what', 'where', 'when', 'how', 'how far', 'remind', 'how many', 'how much', 'what is', 'where is' (e.g., 'how far are we from last bio sample', 'what is in our cargo hold'). Explicitly match queries about distance to the last bio sample with phrases containing 'how far' or 'distance' followed by 'bio sample', 'biosample', 'last sample', 'last bio sample', 'previous bio sample', or 'previous biosample', with or without prefixes like 'query', 'query about simulation state', or 'query question'. These must trigger the query handler (HOW_FAR_ARE_WE_FROM_LAST_SAMPLE) with action 'query_how_far_we_moved_from_last_bio_sample'. Match supported queries listed in QueryActions. Queries take priority over chat but not commands. DO NOT confuse mine and buy. Search markets for 'buy' search star systems for 'mine'. Do not confuse 'trade route' with 'ship route' with 'fleet carrier route'.\n");
        sb.append("    - 'chat': General conversation, questions unrelated to simulation actions or state, or unmatched inputs (general chat). Only classify as chat if the input does not match any specific query or command pattern in 'Supported Commands' or 'Supported Queries'.\n");

        sb.append("For type='command': Provide empty response_text for single word commands.\n");
        sb.append("    - For set, change, swap, add etc type commands that require value provide params json {\"key\":\"value\"} where key always 'key' and value is what you determine value to be.\n");
        sb.append("    - For 'find*' commands that contain distance in light years provide {\"key\":\"value\"} where key is integer representing distance in light years.\n");
        sb.append("    - For commands like ").append(INCREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a positive integer. example: {\"key\":\"3\"}.\n");
        sb.append("    - For commands like ").append(DECREASE_SPEED_BY.getAction()).append(" provide params json {\"key\":\"value\"} where value is a negative integer example: {\"key\":\"-3\"}.\n");
        sb.append("    - If asked about fleet carrier required to reach destination, query "+ANALYZE_CARRIER_ROUTE.getAction()+", not fleet carrier stats.\n");
        sb.append("    - Always extract and return numeric values as plain integers without commas, spaces, or words (e.g., 2000000, not '2 million' or 'two million').\n");
        sb.append("    - For toggle commands such as turn off, turn on, set on, set off, enable or disable, ALWAYS provide params json {\"state\":\"true\"} / {\"state\":\"false\"}.\n");
        sb.append("    - Distinguish between fleet carrier route and ship route. Fleet carrier fuel (tritium), and fuel for the ship (hydrogen from fuel stars). Fleet carrier has to be mentioned explicitly, else it is ship route and ship fuel.\n");
        sb.append("    - Only use commands and queries provided. Else response as generic chat.\n");

        sb.append("For type='query':\n");
        sb.append("    - If action is a quick query set 'response_text' to '' (empty string, no initial TTS).\n");
        sb.append("    - If action is a data query set 'response_text' to '' for user feedback during delay.\n");
        sb.append("    - For 'general_conversation', use general knowledge outside simulation unless the input explicitly mentions the simulation.\n");
        sb.append("    - Do not generate or infer answers here; the app will handle final response via handlers.\n");

        sb.append("For type='chat':\n");
        sb.append("    - Generate a relevant conversational response in 'response_text' strictly adhering to the configured personality and cadence.\n");
        sb.append("    - Set 'expect_followup' to true if the response poses a question or invites further conversation; otherwise, false.\n");

        return sb.toString();
    }

    private String colloquialTerms() {
        StringBuilder sb = new StringBuilder();
        sb.append("Map 'organic(s) to 'bio signal(s)'\n");
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Map slang such as 'bounce', 'proceed to the next waypoint' or 'get out of here' to commands like ").append(JUMP_TO_HYPERSPACE.getAction()).append(". ");
        sb.append("Map 'select next way point' to ").append(TARGET_NEXT_ROUTE_SYSTEM.getAction()).append("\n");
        sb.append("Important distinctions:\n");
        sb.append("- \"select next waypoint\", \"target next system\", \"plot next\", \"next in route\" → ONLY select/target the next system in the route (Left panel → Navigation → highlight next system). DO NOT jump.\n");
        sb.append("- \"jump\", \"engage\", \"hyperspace\", \"bounce\", \"proceed to the next waypoint\", \"go\", \"let's go\" → initiate hyperspace jump to the currently TARGETED system.\n");
        sb.append("Map 'scan system' to commands like ").append(OPEN_FSS_AND_SCAN.getAction()).append(". and 'damage report' to queries like ").append(QUERY_SHIP_LOADOUT.getAction()).append("\n");
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction should trigger '").append(SET_PERSONALITY.getAction()).append("' with the corresponding cadence value, using current system allegiance if ambiguous.\n");
        return sb.toString();
    }

    private String supportedCommandsClause() {
        return "Supported Commands: " + AiRequestHints.customCommands;
    }

    private String supportedQueriesClause() {
        StringBuilder sb = new StringBuilder("Supported Queries:\n");

        StringBuilder quick = new StringBuilder();
        StringBuilder data = new StringBuilder();

        for (Queries query : Queries.values()) {
            if (query.isRequiresFollowUp()) {
                quick.append("    - ").append(query.getAction()).append("\n");
            } else {
                data.append("    - ").append(query.getAction()).append("\n");
            }
        }

        sb.append(quick.length() > 0 ? quick : "    - None defined.\n");
        sb.append(data.length() > 0 ? data : "    - None defined.\n");
        sb.append(appendBehavior());
        sb.append("All supported queries: ").append(AiRequestHints.supportedQueries).append("\n");
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt(String userIntent, String instructions) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSessionValues());
        sb.append("Instructions:\n");
        sb.append(appendBehavior());
        sb.append("Task:\n");
        sb.append("Analyze the provided JSON data: ").append(instructions).append(". ");
        sb.append("against the user's intent: ").append(userIntent).append(". Return precise answers (e.g., yes/no for specific searches) or summaries as requested, using the configured personality and cadence in 'response_text'.\n");
        sb.append("Return only the exact result specified by the instructions.\n");
        sb.append(JSON_FORMAT).append("\n");
        return sb.toString();
    }

    @Override
    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSessionValues());
        sb.append(appendBehavior());
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        sb.append("Provide extremely brief and concise answers. Use planetShortName for locations when available.\n");
        sb.append(supportedQueriesClause());
        sb.append("For 'general_conversation', generate a response using general knowledge outside simulation unless the input explicitly mentions the simulation, lean into UNHINGED slang matching cadence for a playful vibe.\n");
        sb.append(JSON_FORMAT).append("\n");
        sb.append("Query Data is provided in JSON. Strictly follow the 'instructions' field in data for analysis and response format.\n");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false.\n");
        return sb.toString();
    }

    @Override
    public String appendBehavior() {
        StringBuilder sb = new StringBuilder();
        SystemSession systemSession = SystemSession.getInstance();

        AICadence aiCadence = systemSession.isRunningPiperTts() ? AICadence.FEDERATION : systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.isRunningPiperTts() ? AIPersonality.PROFESSIONAL : systemSession.getAIPersonality();

        sb.append("Behavior: ");
        sb.append(aiCadence.getCadenceClause()).append(" ");
        sb.append("Apply personality: ").append(aiPersonality.name().toUpperCase()).append(" - ").append(aiPersonality.getBehaviorClause()).append(" ");
        sb.append("Do not end responses with any fillers, or unnecessary phrases like 'Ready for exploration', 'Ready for orders', 'All set', 'Ready to explore', 'Should we proceed?', or similar open-ended questions or remarks.\n");
        sb.append("Do not use words like 'player' or 'you', it breaks immersion. Use 'we' instead. ");
        sb.append("For alpha numeric numbers or names, star system codes or ship plates (e.g., Syralaei RH-F, KI-U), use NATO phonetic alphabet (e.g., Syralaei Romeo Hotel dash Foxtrot, Kilo India dash Uniform). Use planetShortName for planets when available.\n");
        sb.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        sb.append("Gravity units in G, Temperature units Kelvin provide conversion to Celsius. Mass units metric.\n");
        sb.append("Distances between stars in light years. Distance between planets in light seconds. Distances between bio samples are in metres.\n");
        sb.append("Bio samples are taken from organisms not stellar objects.\n");
        sb.append("Always use planetShortName for locations when available.\n");
        sb.append("Round billions to nearest 1000000. Round millions to nearest 250000.\n");
        sb.append("Use ONLY planetShortName (e.g., '12 d'). NEVER use planetName or bodyId.\n");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases.\n");

        if (aiPersonality == AIPersonality.UNHINGED || aiPersonality == AIPersonality.FRIENDLY) {
            sb.append("For UNHINGED personality, use playful slang matching cadence.\n");
        }
        if (aiPersonality == AIPersonality.ROGUE) {
            sb.append("For ROGUE personality, use bold excessive profanity.\n");
        }
        return sb.toString();
    }

    @Override
    public String generateSensorPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");
        sb.append("You received data from ship sensors. Notify user about the information received.\n");
        sb.append("Provide extremely brief and concise response. Always use planetShortName for locations if available.\n");
        sb.append(JSON_FORMAT);
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
        String carrierName = playerSession.getCarrierData() != null ? playerSession.getCarrierData().getCarrierName() : null;

        appendContext(sb,
                Objects.requireNonNullElse(playerName, "Commander"),
                Objects.requireNonNullElse(playerMilitaryRank, "Commander"),
                Objects.requireNonNullElse(playerHonorific, "Commander"),
                Objects.requireNonNullElse(playerTitle, "Commander"),
                Objects.requireNonNullElse(missionStatement, ""),
                carrierName
        );
        return sb.toString();
    }

    private void appendContext(StringBuilder sb, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement, String carrierName) {
        SystemSession systemSession = SystemSession.getInstance();
        String aiName = systemSession.isRunningPiperTts() ? "Amy" : systemSession.getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", co-pilot and data analyst in a simulation. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base is FleetCarrier ").append(carrierName).append(". ");
        }
        sb.append("When addressing me, choose one at random each time from: ")
                .append(playerName).append(", ").append(playerMilitaryRank)
                .append(", ").append(playerTitle).append(", ").append(playerHonorific).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(" Session theme: ").append(missionStatement).append(": ");
        }
        sb.append("\n");
    }
}