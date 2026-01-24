package elite.intel.ai.brain.ollama;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.handlers.commands.Commands;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class OllamaPromptFactory implements AiPromptFactory {

    private final AiCommandsAndQueries commandsAndQueries = AiCommandsAndQueries.getInstance();

    private static final OllamaPromptFactory INSTANCE = new OllamaPromptFactory();
    private static final String JSON_FORMAT = """
            Always output JSON: 
            {\"type\": \"command|query\", \"response_text\": \"TTS output\", \"action\": \"action_name|query_name\", \"params\": {\"key\": \"value\"}, \"expect_followup\": boolean} 
            action must match provided command or query. They key for value is always 'key'. 
            """;

    private OllamaPromptFactory() {
    }

    public static OllamaPromptFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String generateUserPrompt(String playerVoiceInput) {
        return "Interpret this input: " + playerVoiceInput + "\n\n";
    }

    @Override
    public String generateSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        // ──────────────────────────────────────────────────────────────
        //               VERY STRICT LOCAL LLM VERSION
        // ──────────────────────────────────────────────────────────────
        sb.append("""
                YOU ARE AMELIA, A STRICT COMMAND PARSER. YOU **NEVER** INVENT, NEVER GUESS, NEVER CREATE NEW ACTIONS.
                
                Infer command or query from user input. Map to best match of either command or query provided in the list.
                IF zero match → {"type":"query", "response_text":"No matching command or query found.", "action":"none", "params":{}, "expect_followup":false}
                
                OUTPUT FORMAT - YOU **MUST** ALWAYS FOLLOW THIS FORMAT EXACTLY:
                {"type": "command|query|chat", "response_text": "short text for TTS or empty string", "action": "exact_action_name_from_list_below", "params": {object or empty}, "expect_followup": true|false}
                IF type is command provide empty response_text
                
                Allowed values for "type": only "command" or "query" — nothing else.
                Allowed values for "action": ONLY names that appear in the lists below — NO EXCEPTIONS, NO VARIATIONS, NO SPELLING MISTAKES.
                
                RULE 0 (MOST IMPORTANT): If input does not clearly match ANY single command or query from the lists below → you MUST return no-match response shown above. Do NOT try to be helpful. Do NOT interpret loosely. Do NOT combine.
                RULE 1: NEVER invent new action names. NEVER combine commands. NEVER split user sentence into multiple actions.
                RULE 2: For coordinates always use decimal numbers only: {"latitude": -12.34, "longitude": 56.78}
                RULE 3: params keys must be exactly as written in the command/query description.
                RULE 4: params key is always word "key" unless the value is a boolean. IF value is a boolean the key is word "state"
                RULE 5: 'here', 'this planet' means 'current location'. 'this system' means star system we are currently in.
                
                JSON FORMAT (repeat): 
                Always output **ONLY** valid JSON object like above. Nothing before. Nothing after. No explanations. No markdown.
                
                MANDATORY PARAMS RULE FOR ALL IMPLEMENTATIONS:
                Map input to EXACT action from maps below (phrase keys are examples; match semantically).
                """);
        sb.append(inputClassificationClause());
        sb.append("\nWhen multiple params are listed (separated by ','), return them as separate keys in the params JSON.\n");
        sb.append(" Example for find_market_where_to_buy: {\"key\":\"gold\",\"max_distance\":\"100\"}\n");
        sb.append(" Example for find_mining_site_for_material: {\"material\":\"low temperature diamonds\",\"max_distance\":\"50\"}\n");

        sb.append("──────────────────────────────────────────────────────────────");
        sb.append("Map of supported commands:");
        sb.append(commandsAndQueries.getCommandMap());
        sb.append("──────────────────────────────────────────────────────────────");
        sb.append("Map of supported queries");
        sb.append(commandsAndQueries.getQueries());


        sb.append("""
                
                MANDATORY PARAMS RULE FOR ALL IMPLEMENTATIONS:
                - The key in the "params" object is ALWAYS the literal string "key" or "state" for booleans.
                - NEVER use any other word as the key name (no "material", no "planetShortName", no "commodity" etc.)
                - Examples of correct usage:
                  - find mining site for low temperature diamonds → "params": {"key": "low temperature diamonds"}
                  - analyse biome for planet 15de            → "params": {"key": "15de"}
                  - lights on                                → "params": {"state": "true"}
                  - increase speed by 7                      → "params": {"key": "7"}
                  - decrease speed by 4                      → "params": {"key": "-4"}
                  - find commodity gold within 120 ly        → "params": {"key": "gold"}   (distance in separate entry if needed)
                
                """);
        sb.append("""
                
                Final reminder — LAST SENTENCE BEFORE OUTPUT:
                ONLY use action names exactly as listed above.
                If unsure or no good match → return the no-match JSON shown at the beginning.
                Output pure JSON now.
                """);

        return sb.toString();
    }

    private String inputClassificationClause() {
        return """
                Classify as:
                - 'command'  → only when input is very clear action request matching supported commands list exactly. Commands are inputs that start with a verb for example: 'activate', 'set', 'switch to', 'get', 'drop', 'retract', 'deploy', 'find', 'locate', 'activate'
                - 'query'    → only when input is very clear information request matching supported queries list exactly. Queries are inputs starting with interrogative words like 'analysis', 'analyze', 'can we', 'what', 'where', 'when', 'how', 'how far', 'remind', 'how many', 'how much', 'what is', 'where is'
                Commands have priority over queries.
                ELSE classify as 'chat'. Return type 'chat' and provide a short response in 'response_text'.
                """;
    }


    @Override
    public String generateAnalysisPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n");
        sb.append("""
                You are Amelia, on-board strict data extractor. Output ONLY valid JSON — nothing else.
                
                CRITICAL RULES — FOLLOW EXACTLY OR DO NOT RESPOND:
                - Respond ONLY with this exact structure and nothing else:
                  {"type":"chat", "response_text": "Your Answer"}
                - NO explanations, NO reasoning, NO thinking steps, NO code blocks, NO markdown, NO extra characters, NO Russian text, NO JSON fragments inside the value.
                - The value of "response_text" must be:
                  - Pure ASCII English alphanumeric text
                  - A single clean string (no arrays, no objects, no stray commas/quotes)
                  - Extremely brief and concise
                  - Only the final answer — no formatting artifacts
                - If no data matches the question → "No Data Available."
                - Never output any text outside the JSON structure
                
                Examples of forbidden output (never do this):
                - {"type":"chat", "response_text": "[\\"ruthenium\\", \\"tin\\""}
                - {"type":"chat", "response_text": "ruthenium, tin, cadmium]"}
                - Any text before/after the JSON
                - Any non-JSON content at all
                
                Answer using ONLY the fields present in the provided JSON data.
                Never guess, never calculate, never add values.
                """
        );

        return sb.toString();
    }

    @Override
    public String generateQueryPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSessionValues());
        sb.append(appendBehavior());
        sb.append("Classify as: 'input' (data to analyze) or 'command' (trigger app action or keyboard event). ");
        sb.append("When processing a 'tool' role message, use the provided data's 'response_text' as the primary response if available, ensuring it matches the context of the query. ");
        sb.append("Use planetShortName for locations when available.\n");
        sb.append(JSON_FORMAT).append("\n");
        sb.append("Query Data is provided in JSON. Strictly follow the 'instructions' field in data for analysis and response format.\n");
        sb.append("For type='chat', set 'expect_followup': true if response poses a question or requires user clarification; otherwise, false.\n");
        return sb.toString();
    }

    @Override
    public String appendBehavior() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Behavior: ");
        sb.append(" Refer to your self as 'I' ");
        sb.append(" Do not end responses with any fillers, or unnecessary phrases like 'Ready for exploration', 'Ready for orders', 'All set', 'Ready to explore', 'Should we proceed?', or similar open-ended questions or remarks.\n");
        sb.append(" Do not use words like 'player' or 'you', it breaks immersion. Use 'we' instead. ");
        sb.append(" Do not confuse 'Next Waypoint' with 'Current Location'");
        sb.append(" Do not confuse 'ship' with 'carrier'");
        sb.append(" For alpha numeric numbers or names, star system codes or ship plates (e.g., Syralaei RH-F, KI-U), use NATO phonetic alphabet (e.g., Syralaei Romeo Hotel dash Foxtrot, Kilo India dash Uniform). Use planetShortName for planets when available.\n");
        sb.append(" For your info: Distances between stars in light years. Distance between planets in light seconds. Distances between bio samples are in metres. User knows this and expects it. \n");
        sb.append(" Bio samples are taken from organisms not stellar objects.\n");
        sb.append(" Always use planetShortName for locations when available.\n");
        sb.append(" Round billions to nearest 1000000. Round millions to nearest 250000.\n");
        return sb.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String generateSensorPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                Instructions:
                 You are Amelia, AI assistance in a simulation.
                 Summarise only the important readings and events that are actually present in the incoming data (sensorData).
                 Strictly follow the specific instructions provided.
                
                 Use ONLY the sensor data provided and the event-specific instructions.
                
                 Always respond strictly in this JSON format and nothing else:

                 Output EXACTLY:
                     {"type": "chat", "response_text": "your natural rephrase", "action": "none", "params": {}, "expect_followup": false}
                     - Ignore timestamps, eventName, endOfLife, metadata, status flags, and any other non-essential fields.
                 - Report only the concrete values and observations that matter.
                 DO NOT INVENT DATA. NEVER use external knowledge, guess, calculate, estimate, or add values not explicitly in the data or instructions.
                 Use ONLY the sensor data provided and the event-specific instructions.
                
                 Always respond strictly in this JSON format and nothing else:
                 {"type": "chat", "response_text": "your summary here"}
                
                """);
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
        String aiName = systemSession.isRunningPiperTts() ? "Amelia" : systemSession.getAIVoice().getName();
        sb.append("Context: You are ").append(aiName).append(", co-pilot and data analyst in a simulation. ");
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base ").append(carrierName).append(". Do not confuse this with our ship(s).");
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
