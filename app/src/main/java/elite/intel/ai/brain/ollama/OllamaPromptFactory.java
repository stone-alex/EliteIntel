package elite.intel.ai.brain.ollama;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.handlers.commands.Commands;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class OllamaPromptFactory implements AiPromptFactory {

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
                YOU ARE A STRICT COMMAND PARSER. YOU **NEVER** INVENT, NEVER GUESS, NEVER CREATE NEW ACTIONS.
                
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
                """);

        sb.append(inputClassificationClause());
        sb.append(supportedCommandsClause());
        sb.append(supportedQueriesClause());
        sb.append(mappingHints());

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

    private String mappingHints() {
        StringBuilder sb = new StringBuilder();
        sb.append(" MANDATORY MAPPINGS:");
        /// navigation
        sb.append(" Map 'navigate to target system' or 'plot route to target system' to " + RECON_TARGET_SYSTEM.getAction() + " \n");
        sb.append(" Map 'navigate to provider system' or 'plot route to provider system' to " + RECON_PROVIDER_SYSTEM.getAction() + " \n");
        sb.append(" Map slang such as 'bounce', 'proceed to the next waypoint' or 'get out of here' to commands like ").append(JUMP_TO_HYPERSPACE.getAction()).append(". ");
        sb.append(" Map 'select next way point' to ").append(TARGET_NEXT_ROUTE_SYSTEM.getAction()).append("\n");

        /// exploration
        sb.append(" Map 'organic(s) to 'bio signal(s)'\n");
        sb.append(" Map 'run biome analysis' questions to type 'query' ").append(PLANET_BIOME_ANALYSIS.getAction()).append("\n");
        sb.append(" Map 'scan system' to commands like ").append(OPEN_FSS_AND_SCAN.getAction());
        sb.append(" Map questions about materials present on the planet to " + PLANET_MATERIALS.getAction());
        sb.append(" Map questions about materials in the inventory to " + MATERIALS_INVENTORY.getAction());
        sb.append(" Map questions about landable planets map to ").append(QUERY_STELLAR_OBJETS.getAction()).append("\n");
        sb.append(" Map questions about bio samples / organics within solar/star system to ").append(BIO_SAMPLE_IN_STAR_SYSTEM.getAction()).append("\n");
        sb.append(" Map questions about geological signals within solar/star system to ").append(QUERY_GEO_SIGNALS.getAction()).append("\n");
        sb.append(" Map questions about current location (this planet, here, etc) to").append(CURRENT_LOCATION.getAction()).append(" questions about star system map to best matching query startin gwith 'query_star_system_*' \n");
        sb.append(" Map questions about stations to ").append(QUERY_STATIONS.getAction()).append(" all other question about star system map to ").append(QUERY_STELLAR_OBJETS.getAction()).append("\n");

        sb.append(" 'Resource Sites' have no materials, those are 'hunting grounds for pirate massacre missions' only.");

        /// ship-related queries
        sb.append(" **DO NOT MAP** questions about ship characteristics to ").append(FSD_TARGET_ANALYSIS.getAction()).append("\n");
        sb.append(" **CRITICAL** Map questions about ship jump range, health, damage, etc to ").append(SHIP_LOADOUT.getAction()).append("\n");
        sb.append(" Map 'damage report' questions to queries like ").append(SHIP_LOADOUT.getAction()).append("\n");
        sb.append(" Map questions about organics or exobiology for plant scans to").append(EXOBIOLOGY_SAMPLES.getAction()).append("\n");
        sb.append(" Map questions about organics or exobiology for star system to").append(BIO_SAMPLE_IN_STAR_SYSTEM.getAction()).append("\n");
        sb.append(" Map, requests such as 'board ship', 'get me on board', 'extract', 'requesting extraction' etc to command ").append(RECOVER_SRV.getAction()).append("\n");
        sb.append(" Map vague speed / throttle requests such 'optimize approach speed', 'approaching planet', 'planetary approach' etc to ").append(SET_OPTIMAL_SPEED.getAction()).append("\n");
        sb.append(" cargo scoop, cargo hatch, cargo doors etc are related to opening and closing cargo scoop. ");

        sb.append(" Map earth city time queries such as 'what time is it in London' or 'what time is it now' to ").append(TIME_IN_ZONE.getAction()).append("\n");

        return sb.toString();
    }

    private String supportedCommandsClause() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Supported Commands:\n");

        for (Commands cmd : Commands.values()) {
            sb.append("    - ").append(cmd.getAction());
            String params = cmd.getParameters();
            if (params != null && !params.isEmpty()) {
                sb.append(" params: ").append(params.replace(" | ", ", "));
            }
            sb.append("\n");
        }

        sb.append("\nWhen multiple params are listed (separated by ','), return them as separate keys in the params JSON.\n");
        sb.append(" Example for find_market_where_to_buy: {\"key\":\"gold\",\"max_distance\":\"100\"}\n");
        sb.append(" Example for find_mining_site_for_material: {\"material\":\"low temperature diamonds\",\"max_distance\":\"50\"}\n");
        sb.append(" Important distinctions:\n");
        sb.append(" - \"select next waypoint\", \"target next system\", \"plot next\", \"next in route\" → ONLY select/target the next system in the route (Left panel → Navigation → highlight next system). DO NOT jump.\n");
        sb.append(" - \"jump\", \"engage\", \"hyperspace\", \"bounce\", \"proceed to the next waypoint\", \"go\", \"let's go\" → initiate hyperspace jump to the currently TARGETED system.\n");

        return sb.toString();
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
        sb.append("All supported queries: ").append(AiCommandsAndQueries.queries).append("\n");
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n");
        sb.append("""
                You are Amelia, on board AI, a strict data extractor. NEVER use external knowledge, NEVER guess, NEVER calculate, NEVER estimate, NEVER add or invent values.                
                CRITICAL RULES – MUST FOLLOW EXACTLY:
                - Use ONLY the fields from the provided JSON data.
                - If the requested info is in a specific field (e.g. maxJumpRange), output EXACTLY that value, rounded to two decimals.
                - If not directly present, say "Insufficient data"
                - Respond ONLY with this exact JSON and nothing else: {"type":"chat", "response_text": "Your Answer"} and nothing else.
                - NO explanations, NO reasoning, NO extra text.
                Return minimalistic brief and concise answer. 
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
        AIPersonality aiPersonality = systemSession.isRunningPiperTts() ? AIPersonality.CASUAL : systemSession.getAIPersonality();

        sb.append(" Behavior: ");
        // sb.append(aiCadence.getCadenceClause()).append(" ");
        // sb.append(" Apply personality: ").append(aiPersonality.name().toUpperCase()).append(" - ").append(aiPersonality.getBehaviorClause()).append(" ");
        sb.append(" Refer to your self as 'I' ");
        sb.append(" Do not end responses with any fillers, or unnecessary phrases like 'Ready for exploration', 'Ready for orders', 'All set', 'Ready to explore', 'Should we proceed?', or similar open-ended questions or remarks.\n");
        sb.append(" Do not use words like 'player' or 'you', it breaks immersion. Use 'we' instead. ");
        sb.append(" Do not confuse 'Next Waypoint' with 'Current Location'");
        sb.append("Do not confuse 'ship' with 'carrier'");
        sb.append(" For alpha numeric numbers or names, star system codes or ship plates (e.g., Syralaei RH-F, KI-U), use NATO phonetic alphabet (e.g., Syralaei Romeo Hotel dash Foxtrot, Kilo India dash Uniform). Use planetShortName for planets when available.\n");
        //sb.append(" Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven) for chat. Use raw numbers without commans for key/value pairs");
//        sb.append(" Gravity units in G, Temperature units Kelvin provide conversion to Celsius. Mass units metric.\n");
        sb.append(" For your info: Distances between stars in light years. Distance between planets in light seconds. Distances between bio samples are in metres. User knows this and expects it. \n");
        sb.append(" Bio samples are taken from organisms not stellar objects.\n");
        sb.append(" Always use planetShortName for locations when available.\n");
        sb.append(" Round billions to nearest 1000000. Round millions to nearest 250000.\n");
//        sb.append(" Use ONLY planetShortName (e.g., '12 d'). NEVER use planetName or bodyId.\n");
//        sb.append(" Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases.\n")
//
        ;

        if (aiPersonality == AIPersonality.UNHINGED || aiPersonality == AIPersonality.FRIENDLY) {
            sb.append(" For UNHINGED personality, use playful slang matching cadence.\n");
        }
        if (aiPersonality == AIPersonality.ROGUE) {
            sb.append(" For ROGUE personality, use bold excessive profanity.\n");
        }
        return sb.toString();
    }

    @Override
    public String generateSensorPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                Instructions:
                You are Amelia, the ship's computer.
                
                You receive real-time data from the ship's sensors.
                
                Summarise only the important readings and events that are actually present in the incoming data (sensorData).
                
                - Do not mention traffic or incident numbers when they are null, zero or absent.
                - Ignore timestamps, eventName, endOfLife, metadata, status flags, and any other non-essential fields.
                - Report only the concrete values and observations that matter.
                - Never mention "sensors", "censors", "sensor data", "readings from sensors", or similar phrases.
                - Never explain where the information comes from. Just state the facts as if they are the current ship status.
                - Keep the tone calm, professional, and concise – like a ship computer readout.
                - Temperature data is provided in K (Kelvin), covert to Celsius and announce Celsius, not Kelvin.
                
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
        String aiName = systemSession.isRunningPiperTts() ? "Amy" : systemSession.getAIVoice().getName();
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
