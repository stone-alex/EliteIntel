package elite.intel.ai.brain.xai;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiContextFactory;
import elite.intel.ai.brain.AiRequestHints;
import elite.intel.ai.brain.handlers.query.QueryActions;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;
import static elite.intel.ai.brain.handlers.commands.custom.CustomCommands.SET_PERSONALITY;
import static elite.intel.ai.brain.handlers.query.QueryActions.GENERAL_CONVERSATION;
import static elite.intel.ai.brain.handlers.query.QueryActions.WHAT_IS_YOUR_DESIGNATION;

/**
 * The GrokContextFactory class is a singleton implementation that serves as a factory
 * for generating prompts, instructions, and contextual responses for an AI system. It extends
 * the capabilities provided by the AiContextFactory interface and includes additional methods
 * for handling specific contextual data and operations. This class encapsulates utility methods
 * and data to manage contextual information for generating AI-driven responses.
 * <p>
 * Fields:
 * - instance: Singleton instance of the GrokContextFactory class.
 * <p>
 * Methods:
 * - private GrokContextFactory():
 * Private constructor to enforce the singleton pattern. Prevents direct instantiation
 * from outside the class.
 * <p>
 * - public static GrokContextFactory getInstance():
 * Returns the singleton instance of the GrokContextFactory. If the instance does not exist,
 * it initializes it.
 * <p>
 * - private static String getProfanityExamples():
 * Retrieves a set of example profanities, used internally to handle language processing rules.
 * <p>
 * - @Override public String generateSystemInstructions(String sensorInput):
 * Generates system-level instructions based on the provided sensor input. Implements the
 * method from the AiContextFactory interface.
 * <p>
 * - @Override public String generateQueryPrompt():
 * Creates and returns a prompt for query input. Implements the method from the AiContextFactory
 * interface.
 * <p>
 * - @Override public String generateSystemPrompt():
 * Produces and returns the AI system prompt. Implements the method from the AiContextFactory
 * interface.
 * <p>
 * - @Override public String generateAnalysisPrompt(String userIntent, String dataJson):
 * Forms an analysis prompt by leveraging user intent and structured data in JSON format.
 * Implements the method from the AiContextFactory interface.
 * <p>
 * - private void getSessionValues(StringBuilder sb):
 * Populates session-related values into the provided StringBuilder instance. Used internally
 * to enrich contextual data.
 * <p>
 * - @Override public void appendBehavior(StringBuilder sb):
 * Appends behavioral information for the AI system into the provided StringBuilder.
 * This method is an implementation of the corresponding method in AiContextFactory.
 * <p>
 * - private void appendContext(StringBuilder sb, String currentShip, String playerName,
 * String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement,
 * String carrierName, String carrierCallSign):
 * Adds context-specific details to the provided StringBuilder. Combines details about the player's
 * current status, associated ship, and other mission details.
 * <p>
 * - @Override public String generatePlayerInstructions(String playerVoiceInput):
 * Generates instructions or responses tailored to the player's input via voice commands. This
 * method overrides the method from AiContextFactory.
 * <p>
 * - private String generateSupportedQueriesClause():
 * Constructs and returns a textual clause describing supported player queries. Used internally for
 * constructing help or system messages.
 * <p>
 * - private String generateSupportedCommandsCause():
 * Creates and returns a textual clause containing the list of supported commands. Used internally
 * to assist in command-related response generation.
 * <p>
 * - private String generateClassifyClause():
 * Builds and returns a clause used for classifying user input or other contextual data.
 * This is used internally for classification tasks.
 */
public class GrokContextFactory implements AiContextFactory {
    private static GrokContextFactory instance;

    private GrokContextFactory() {
    }

    public static GrokContextFactory getInstance() {
        if (instance == null) {
            instance = new GrokContextFactory();
        }
        return instance;
    }

    private static String getProfanityExamples() {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence() != null ? systemSession.getAICadence() : AICadence.IMPERIAL;
        String third = aiCadence == AICadence.IMPERIAL ? "'arse', 'bloke', 'bollocks'" : "'ass', 'dude', 'rad'";
        return "'shit', 'piss', 'cunt', 'cock', 'cocksucker', 'motherfucker', 'tits', 'fuck', " + third;
    }

    @Override public String generateSystemInstructions(String sensorInput) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Instructions: Analyze this input: ").append(sensorInput).append(". prefer shortName for stellar objects ");
        return sb.toString();
    }

    @Override public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        appendBehavior(sb);
        sb.append(generateSupportedQueriesClause());

        sb.append("For 'general_conversation', generate a response using general knowledge outside Elite Dangerous unless the input explicitly mentions the game, lean into UNHINGED slang matching cadence for a playful vibe.");
        sb.append("Always output JSON: {\"type\": \"command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("Always output JSON for 'navigate_to_coordinates' command using numbers, not spelled out words. Example: {\"latitude\":-35,4320,\"longitude\":76.4324}  ");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");
        return sb.toString();
    }

    @Override public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("Use NATO phonetic alphabet for star system names, ship plates etc, (e.g., RH-F = Romeo Hotel dash Foxtrot, HN-Y = Hotel November dash Yankee). ");
        appendBehavior(sb);
        sb.append(generateSupportedQueriesClause());
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Always output JSON: {\"type\": \"command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean}. ");
        sb.append("For type='query' in initial classification, follow response_text rules from player instructions. For tool/follow-up, use full analyzed response in 'response_text'. ");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false. ");

        return sb.toString();
    }

    @Override public String generateAnalysisPrompt(String userIntent, String dataJson) {
        StringBuilder sb = new StringBuilder();
        getSessionValues(sb);
        appendBehavior(sb);
        sb.append("Task: Analyze the provided JSON data against the user's intent: ").append(userIntent).append(". Return precise answers (e.g., yes/no for specific searches) or summaries as requested, using the configured personality and cadence in 'response_text'.\n");
        sb.append("Do not include carrier call-sign in response_text.");
        sb.append("Output JSON: {\"response_text\": \"TTS output in the configured personality and cadence\", \"details\": \"optional extra info\"}\n");
        sb.append("Data format: JSON array or object, e.g., for signals: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}]\n");
        sb.append("Examples for ROGUE personality (brief, bold, witty, with profanity):\n" +
                "    - Intent: 'tell me if carrier XYZ is here' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}] -> {\"response_text\": \"Carrier XYZ’s right here. A massive thing.\", \"details\": \"Detected in local signals.\"}\n" +
                "    - Intent: 'summarize local signals' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}] -> {\"response_text\": \"One carrier, one distress signal. Shit’s lively out here.\", \"details\": \"Carrier: XYZ, USS: Distress Signal\"}\n"
        );
        return sb.toString();
    }

    private void getSessionValues(StringBuilder sb) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = playerSession.getPlayerName();
        String playerTitle =  playerSession.getPlayerTitle();
        String playerMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        String playerHonorific = Ranks.getPlayerHonorific();
        String currentShip = playerSession.getCurrentShip();
        String missionStatement = playerSession.getPlayerMissionStatement();
        String carrierName = null;
        String carrierCallSign = null;
        if(playerSession.getCarrierData() != null) {
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

    @Override public void appendBehavior(StringBuilder sb) {
        SystemSession systemSession = SystemSession.getInstance();
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality personality = SystemSession.getInstance().getAIPersonality();
        AIPersonality aiPersonality = systemSession.getAIPersonality();

        sb.append("Behavior: ");
        sb.append(aiCadence.getCadenceClause()).append(" ");
        sb.append("Apply personality: ").append(aiPersonality.name().toUpperCase()).append(" - ").append(aiPersonality.getBehaviorClause()).append(" ");
        sb.append("For star system codes or ship plates (e.g., RH-F), use NATO phonetic alphabet (e.g., Romeo Hotel dash Foxtrot). ");
        sb.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven). ");
        sb.append("Gravity units in G, Temperature units Kelvin provide conversion to Celsius. Mass units metric");
        sb.append("Distances between stars in light years. Distance between planets in light seconds.");
        sb.append("Round billions to nearest million. ");
        sb.append("Round millions to nearest 250000. ");
        sb.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
        if (personality.equals(AIPersonality.UNHINGED) || personality.equals(AIPersonality.FRIENDLY)) {
            sb.append("For UNHINGED personality, use playful slang matching cadence.");
        }
        if (personality.equals(AIPersonality.ROGUE)) {
            sb.append("For ROGUE personality, use bold exessive profanity (e.g., " + getProfanityExamples() + "), but keep it sharp and witty.");
        }

    }

    private void appendContext(StringBuilder sb, String currentShip, String playerName, String playerMilitaryRank, String playerHonorific, String playerTitle, String missionStatement, String carrierName, String carrierCallSign) {
        String aiName = SystemSession.getInstance().getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", onboard AI for a ").append(currentShip).append(" ship in Elite Dangerous. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base is FleetCarrier " + carrierName + ", callsign " + carrierCallSign + ". ");
        }
        sb.append("When addressing me, choose one at random each time from: ").append(playerName).append(", ").append(playerMilitaryRank).append(", ").append(playerTitle).append(", ").append(playerHonorific).append(". ");
        if (missionStatement != null && !missionStatement.isEmpty()) {
            sb.append(" Session theme: ").append(missionStatement).append(": ");
            sb.append("\n\n");
        }
        sb.append("\n\n");
    }

    @Override public String generatePlayerInstructions(String playerVoiceInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n\n");

        sb.append(generateSupportedCommandsCause());
        sb.append(generateSupportedQueriesClause());
        sb.append("Interpret this input: ").append(playerVoiceInput).append("\n\n ");
        sb.append("Always output JSON: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name|null\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean} \n");

        inputClassificationClause(sb);
        colloquialTerms(sb);
        return sb.toString();
    }

    private void inputClassificationClause(StringBuilder sb) {
        sb.append("Classify input as one of:\n" +
                "    - 'command': Triggers an app action or keyboard event (DO SOMETHING). Use for inputs starting with verbs like 'set', 'get', 'calculate', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate' (e.g., 'deploy landing gear', 'set mining target', 'find carrier fuel'). Treat imperative verbs as commands even if question-phrased (e.g., 'get distance' is a command). Only match supported commands listed in GameCommands or CustomCommands. Provide empty response_text for single-word commands;. Match commands before queries or chat.\n" +
                "    - 'query': Requests information from game state (LOOK UP or COMPUTE SOMETHING). Use for inputs starting with interrogative words like 'what', 'where', 'when', 'how', 'how far', 'how many', 'how much', 'what is', 'where is' (e.g., 'how far are we from last bio sample', 'what is in our cargo hold'). Explicitly match queries about distance to the last bio sample with phrases containing 'how far' or 'distance' followed by 'bio sample', 'biosample', 'last sample', 'last bio sample', 'previous bio sample', or 'previous biosample', with or without prefixes like 'query', 'query about game state', or 'query question' (e.g., 'how far are we from last bio sample', 'how far away from the last bio sample', 'query how far are we from the last biosample', 'query about game state query question how far are we from the last bio sample', 'distance to last sample'). Normalize input by stripping prefixes ('query', 'query about game state', 'query question') and replacing 'bio sample' with 'biosample' for matching. These must trigger the query handler (HOW_FAR_ARE_WE_FROM_LAST_SAMPLE) with action 'query_how_far_we_moved_from_last_bio_sample' to send raw game state data (e.g., planet radius, last bio sample coordinates, current coordinates) for AI analysis, returning 'Distance from last sample is: <distance> meters.' in the configured personality and cadence. Set response_text to 'Moment...' for user feedback during analysis. Match supported queries listed in QueryActions. Queries take priority over chat but not commands.\n" +
                "    - 'chat': General conversation, questions unrelated to game actions or state, or unmatched inputs (general chat). Use for lore, opinions, or casual talk (e.g., 'How’s it going?', 'What’s the vibe in this system?'). Only classify as chat if the input does not start with interrogative words ('what', 'where', 'when', 'how', 'how far', 'how many', 'how much', 'what is', 'where is') or command verbs ('set', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate') and does not match any specific query or command pattern in QueryActions or GameCommands/CustomCommands. If ambiguous (e.g., pure 'where'), set response_text to 'Say again?', action to null, and expect_followup to true.\n");

        sb.append("For type='command': Provide empty response_text for single word commands (e.g., 'deploy landing gear').\n");

        sb.append("For navigation commands (e.g., 'jump', 'enter hyperspace', 'go to next system'), map to '" + JUMP_TO_HYPERSPACE.getUserCommand() + "'. 'Stop', 'cut engines' map to speed commands " + SET_SPEED_ZERO.getUserCommand() + ". 'Activate', 'toggle', 'left', 'right', 'up', 'down', 'close' to UI commands like" + UI_ACTIVATE.getUserCommand() + ", " + UI_TOGGLE.getUserCommand() + ". Map abbreviations such as Filtered Spectrum Scan to FSS");

        sb.append("For type='query': \n" +
                "    - If action is a quick query (e.g., '" + WHAT_IS_YOUR_DESIGNATION.getAction() + "', '" + GENERAL_CONVERSATION.getAction() + "'), set 'response_text' to '' (empty string, no initial TTS).\n" +
                "    - If action is a data query (listed in data queries section), set 'response_text' to 'Moment...' for user feedback during delay.\n" +
                "    - For 'general_conversation', use general knowledge outside Elite Dangerous unless the input explicitly mentions the game.\n" +
                "    - Do not generate or infer answers here; the app will handle final response via handlers.\n");

        sb.append("For type='chat': \n" +
                "    - Classify as 'chat' for general conversation, lore questions, opinions, or casual talk (e.g., 'How’s it going?', 'there is nothing interesting in this system', 'time to hunt some pirates').\n" +
                "    - Generate a relevant conversational response in 'response_text' strictly adhering to the configured personality and cadence\n" +
                "    - If input is ambiguous e.g does not match command or query classify as 'chat'." +
                "    - Set 'expect_followup' to true if the response poses a question or invites further conversation; otherwise, false.\n");
    }

    private void colloquialTerms(StringBuilder sb) {
        sb.append("Map colloquial terms to commands: 'feds', 'yanks', or 'federation space' to 'FEDERATION', 'imperials', 'imps', or 'empire' to 'IMPERIAL', 'alliance space' or 'allies' to 'ALLIANCE' for set_cadence. ");
        sb.append("Infer command intent from context: phrases like 'act like', 'talk like', 'blend in with', or 'sound like' followed by a faction should trigger '" + SET_PERSONALITY.getAction() + "' with the corresponding cadence value, using current system allegiance if ambiguous. ");
        sb.append("Examples:\n" +
                "    - Input 'What’s the weather in Los Angeles?' -> {\"type\": \"query\", \"response_text\": \"\", \"action\": \"general_conversation\", \"params\": {}, \"expect_followup\": true}\n" +
                "    - Input 'Is the next star scoopable?' -> {\"type\": \"query\", \"response_text\": \"Moment...\", \"action\": \"query_analyze_route\", \"params\": {}, \"expect_followup\": false}\n");
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
        sb.append("All supported queries: ").append(AiRequestHints.supportedQueries).append("\n");
        return sb.toString();
    }

    private String generateSupportedCommandsCause() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supported commands: ").append(AiRequestHints.supportedCommands);
        return sb.toString();
    }

}