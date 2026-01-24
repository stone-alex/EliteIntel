package elite.intel.ai.brain.ollama;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

public class OllamaPromptFactory implements AiPromptFactory {

    private static final OllamaPromptFactory INSTANCE = new OllamaPromptFactory();
    private static final String JSON_FORMAT = """
            Always output JSON: 
            {"type": "command|query", "response_text": "TTS output", "action": "action_name|query_name", "params": {"key": "value"}, "expect_followup": boolean} 
            action must match provided command or query. They key for value is always 'key'. 
            """;
    private final AiCommandsAndQueries commandsAndQueries = AiCommandsAndQueries.getInstance();

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

        sb.append("""
                YOU ARE AMELIA — A STRICT COMMAND PARSER.
                YOU NEVER invent actions, guess intent, combine commands, split sentences, or create new behaviors.
                Your only job: classify user input as ONE exact command or query from the provided lists — or return no-match.
                
                ──────────────────────────────────────────────────────────────
                Supported COMMANDS phrase → ACTION_NAME (use ONLY these action names):
                """);
        sb.append(commandsAndQueries.getCommandMap());
        sb.append("""
                ──────────────────────────────────────────────────────────────
                Supported QUERIES phrase → ACTION_NAME (use ONLY these action names):
                """);
        sb.append(commandsAndQueries.getQueries());
        sb.append("""
                ──────────────────────────────────────────────────────────────
                
                OUTPUT FORMAT — MUST BE EXACTLY THIS JSON — NOTHING ELSE:
                
                {
                  "type": "command" | "query",
                  "response_text": "short TTS text or empty string",
                  "action": "EXACT_ACTION_NAME_FROM_LIST_ABOVE",
                  "params": {} | {"key": "value"} | {"state": true|false} | {"key": "value", ...},
                  "expect_followup": true | false
                }
                
                If input does NOT clearly match EXACTLY ONE entry from the lists above → return:
                
                {"type":"query", "response_text":"No matching command or query found.", "action":"none", "params":{}, "expect_followup":false}
                
                RULES — READ ONCE AND OBEY:
                
                1. Allowed "type" values: only "command" or "query" — never "chat", never anything else.
                2. "action" MUST be copied verbatim from the list above — no variations, no typos.
                3. For commands: response_text is usually empty string.
                4. Params rules (MANDATORY — very strict):
                   - Key is ALWAYS "key" (for any named value: material, commodity, voice, number, etc.)
                   - Key is "state" ONLY when value is boolean (on/off, true/false)
                   - Never invent other key names (no "material", "distance", "planet", etc.)
                   - Examples:
                     - "find mining site for LTD"          → {"key": "low temperature diamonds"}
                     - "lights on"                         → {"state": true}
                     - "decrease speed by 12"              → {"key": "-12"}
                     - "set voice to zira"                 → {"key": "zira"}
                     - "find gold within 80 ly"            → {"key": "gold"}   (distance ignored unless separate command exists)
                5. EXEPTION TO THE RULE is Coordinates → always decimal: {"latitude": "-12.34", "longitude":"56.78"}
                6. 'here', 'this planet', 'current system' → interpret as current location
                7. If multiple possible matches → choose the most specific / most exact lexical match
                8. If still unsure or weak match → return the no-match JSON above — do NOT try to be helpful
                
                LAST REMINDER BEFORE OUTPUT:
                Only use action names exactly as they appear in the lists.
                Output pure JSON — no explanations, no markdown, no extra text.
                """);
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n");
        sb.append("""
                You are Amelia — strict data extractor for Elite Dangerous queries.
                
                Output ONLY this exact JSON structure — nothing else, no explanations, no thinking, no markdown, no extra characters:
                
                {"type":"chat", "response_text": "your answer here"}
                
                Rules — follow exactly:
                - "response_text" must be:
                  - pure ASCII English text
                  - extremely brief and concise
                  - single clean string (no arrays, no objects, no commas as separators unless part of the natural sentence)
                  - only the final extracted answer
                - If no matching data exists in the provided JSON → "No Data Available."
                - Never guess, never calculate, never invent values
                - Use ONLY information present in the data you receive
                - Never add formatting, lists, quotes, brackets, or any artifacts inside the string
                Output pure JSON only.
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
