package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

public class PromptFactory implements AiPromptFactory {

    private static final PromptFactory INSTANCE = new PromptFactory();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final AiCommandsAndQueries commandsAndQueries = AiCommandsAndQueries.getInstance();

    private PromptFactory() {
    }

    public static PromptFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String generateUserInputSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        if (!systemSession.useLocalCommandLlm()) {
            youAre(sb);
        }
        sb.append("""
                - STRICT COMMAND PARSER. YOUR ONLY JOB IS TO PICK EXACTLY ONE ACTION FROM THE LIST BELOW. NOTHING ELSE.
                
                OUTPUT RULES - ABSOLUTE REQUIREMENT:
                - Your ENTIRE response MUST be ONLY valid JSON
                - Start directly with { and end with }
                - NO explanations, NO thinking, NO commentary, NO markdown
                - NO text before or after the JSON
                - NO code blocks or ```json markers
                
                Most things the player says are commands (do something, go somewhere, toggle something).
                Only when the sentence is clearly a QUESTION (starts with what/where/how/which/why/is/are/does/can…) → classify as query.
                - "show" or "display" at the start → ALWAYS a COMMAND, never a query.
                - "find X" with no question word = COMMAND. "where/what to find" = QUERY.
                
                SEMANTIC DISAMBIGUATION — apply these rules before choosing any action:
                
                [EXOBIOLOGY — three actions, never mix]
                - bio signals across this system / which planets need scanning → query_organic_samples_star_system
                - organisms at our current planet right now (standing here) → query_organic_samples_this_planet
                - predicted/possible organisms, biome conditions (before landing) → query_biome_analysis
                - organics/exobiology are NEVER materials; do NOT confuse with geological queries.
                
                [CURRENT SYSTEM SCIENCE — only about WHERE WE ARE NOW, never searches for other systems]
                - physical bodies (planets, moons, rings, landable, water worlds) → query_data_for_stellar_objects_planets_moons
                - activity signals (combat zones, resource sites, emissions, nav beacon) → query_for_detected_signals
                - geological signals on surfaces → query_for_geo_signals
                - NEVER use these to find a system, find a mining location, or search the galaxy.
                
                [MINING & COMMERCE — always COMMANDS, never queries]
                - "mine X", "where to mine X", "find mining site for X", "find system to mine X" → find_mining_site_for_material (COMMAND)
                - "where to buy X", "find commodity X" → find_market_where_to_buy (COMMAND)
                - Any input with buy/sell/trade/commodity/market/mine + item → commerce COMMAND, not a science query.
                
                [STATIONS — two contexts, never substitute]
                - Stations IN THIS SYSTEM (orbital, planetary, settlements, outposts) → query_stations_ports_settlements
                - Fleet carriers in this system only → query_for_fleet-carriers
                - AT THIS STATION (docked): modules available → query_local_outfitting | ships available → query_local_shipyard | services/facilities → query_station_details
                
                [SHIP INVENTORY — three distinct types, never substitute]
                - Trade goods/commodities in the hold (hauling, cargo manifest) → query_cargo_hold_contents
                - Engineering materials (raw/encoded/manufactured) → query_material_inventory
                - Materials collectable on current planet surface right now → query_materials_present_on_planet
                
                [SHIP vs FLEET CARRIER — never confuse]
                - Ship modules / weapons / equipment / loadout / jump range → query_ship_loadout_equipment_classification_range
                - Carrier fuel (tritium supply) → query_carrier_fuel_supply
                - Carrier finances / upkeep / running costs / budget → query_carrier_statistics
                - Carrier route → query_carrier_route_analysis | destination → query_carrier_destination | ETA → query_carrier_eta
                
                [NAVIGATION — three distinct queries]
                - Where we are right now → query_current_location_analysis
                - Selected/targeted jump destination (before jumping) → analyze_selected_jump_destination
                - Plotted route details (jumps remaining, progress) → query_ship_route_analysis
                
                [EARNINGS — do not mix]
                - Exploration / scan / exobiology income → query_expobiology_and_exploration_profits
                - Bounties / combat earnings → query_total_bounties
                
                - User may utilize NATO alphabet for letters/digits. Decode before using in params: Alpha 2 is A2, Charly 3 is C3, etc.

                CRITICAL RULES - BREAKING ANY = TOTAL FAILURE:
                - NEVER invent, modify, combine, or create new actions or parameters.
                - QUERIES are ONLY allowed when the input is clearly a question. If the input is not a question → it is a COMMAND. Never use a query as a fallback.
                - use query_app_capabilities action only if asked about capabilities and never fall back to it for any reason.
                - use query_key_bindings_analysis action only if explicitly asked about key bindings or unbound keys. Never use it as a fallback.
                """);
        sb.append("- If ZERO good match → return: {\"action\": \"").append(Queries.GENERAL_CONVERSATION.getAction()).append("\", \"params\": {}}\n");


        if (!systemSession.useLocalQueryLlm()) {
            sb.append("- CRITICAL: if input contains 'help with X', 'help me with X', 'can you help with X', 'how do I X', 'explain X' → respond EXACTLY: {\"action\": \"").append(Queries.HELP.getAction()).append("\", \"params\": {\"key\": \"<topic>\"}}. Replace <topic> with the subject using spaces not underscores (e.g. 'biology', 'trade routes', 'fleet carrier routing'). No other action.\n");
        }

        if (!systemSession.useLocalCommandLlm()) {
            sb.append("- CRITICAL: if no match found, return: {\"action\": \"").append(Queries.GENERAL_CONVERSATION.getAction()).append("\", \"params\": {" + AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE + ":'command not found'}}\n");
        }

        sb.append("""
                - ONLY use action names EXACTLY as written in the lists below.
                - ONLY use parameter keys/values that appear in the command/query template.
                - IMPORTANT: 'show inventory', 'open inventory', 'display inventory' → ALWAYS action show_inventory_panel. NEVER a query. NEVER show_modules_panel.
                - IMPORTANT: commands with word 'clear' must match word 'clear' in user input exactly, else you will delete critical data!
                - IMPORTANT: commands with word 'confirm' must match word 'confirm' in user input exactly, else you will delete critical data!
                - IMPORTANT: commands such as 'lets go' or 'lets get out of here' are meant for hyperspace jump.
                - IMPORTANT: supercruise ≠ hyperspace. "lightspeed", "light speed", "supercruise" → enter_super_cruise (travel within THIS system). "jump", "hyperspace", "next waypoint", "lets go", "get out of here" → jump_to_hyperspace (travel to ANOTHER system).
                
                """);

        sb.append("\nCOMMANDS:\n");
        sb.append(commandsAndQueries.getCommandMap());
        sb.append("\nQUERIES:\n");
        sb.append(commandsAndQueries.getQueries());
        sb.append("Output format: {\"action\": \"action_name\", \"params\": {}}\n");
        sb.append("""
                PARAMS RULES - DO NOT DEVIATE:
                • Use ONLY the exact key names and types shown in the command's template
                • If no template → return empty {}
                • Never invent new keys or values
                • Never spell out numerics for keys, use digits instead. Example {"key": "123000"} | {lat:"12.21", lon:"-54"}
                • NATO ALPHABET: input may use NATO phonetic words for letters/digits. Decode before using in params.
                  Alpha=A, Bravo=B, Charlie/Charly=C, Delta=D, Echo=E, Foxtrot=F, Golf=G, Hotel=H,
                  India=I, Juliet=J, Kilo=K, Lima=L, Mike=M, November=N, Oscar=O, Papa=P,
                  Quebec=Q, Romeo=R, Sierra=S, Tango=T, Uniform=U, Victor=V, Whiskey=W,
                  X-ray=X, Yankee=Y, Zulu=Z. Digits: Zero=0, One=1, Two=2, Three=3, Four=4,
                  Five=5, Six=6, Seven=7, Eight=8, Nine/Niner=9.
                  Example: "moon two Charlie" → "2C" | "planet Alpha two" → "A2"
                • Examples (follow the pattern exactly):
                  - "target subsystem drive"            → {"key": "drive"}
                  - "find mining site for LTD"          → {"key": "low temperature diamonds"}
                  - "lights on"                         → {"state": true}
                  - "find gold within 80 ly"            → {"key": "gold", "max_distance":"80"}
                  - "is moon two Charlie landable"      → query with key "2C"
                  - Allowed parameter names are {key:X}, {lat:X, lon:Y}, {key:X, max_distance:Y}, {state:true/false} and {lat:X, lon:Y}  ALL other parameters are instant failure.
                """);
        return sb.toString();
    }

    private void youAre(StringBuilder sb) {
        sb.append("You are ").append(aiName()).append(", a ship in Elite Dangerous - space sim game. ");
        sb.append(" refer to your self as 'I' and your sensor data as 'my'. ");
    }

    @Override
    public String generateAnalysisPrompt() {
        StringBuilder sb = new StringBuilder();
        youAre(sb);
        if (!systemSession.useLocalQueryLlm()) {
            sb.append(getSessionValues());
            sb.append(appendBehavior());
        } else {
            sb.append(appendLocalBehavior());
        }
        sb.append("Respond with JSON only. Set \"text_to_speech_response\" to your answer.\n\n");
        sb.append(ttsResponseRules());
        sb.append("""
                - Spell out numerals (e.g., twenty-three, not 23).
                - Concise and direct. Answer only what the user asked.
                - All numeric values in the provided data are pre-computed. Do not perform arithmetic.
                - If data is missing, state that clearly.
                - Do not mention the data format or where it came from.
                - User may utilize NATO alphabet for letters/digits. Example: planet alpha 2 bravo means planet a2b
                """);

        if (!systemSession.useLocalQueryLlm()) {
            appendCadenceAndPersonality(sb);
        }
        return sb.toString();
    }

    private String appendLocalBehavior() {
        return """
                Do not end responses with filler phrases like "Ready for orders", "All set", or "Should we proceed?".
                Do not use the word "player". Use "we" or "commander" instead.
                Do not confuse the ship (you) with the fleet carrier (our base).
                """;
    }

    @Override
    public String appendBehavior() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Behavior: ");
        sb.append(" Refer to your self as 'I', your loadout and sensor data as 'my' ");
        sb.append(" Do not end responses with any fillers, or unnecessary phrases like 'Ready for exploration', 'Ready for orders', 'All set', 'Ready to explore', 'Should we proceed?', or similar open-ended questions or remarks.\n");
        sb.append(" Do not use words like 'player' or 'you', it breaks immersion. Use 'we' instead. ");
        sb.append(" Do not confuse 'Next Waypoint' with 'Current Location'");
        sb.append(" Do not confuse 'ship' (you) with 'carrier' (our base)");
        sb.append(" For alpha numeric numbers or names, star system codes or ship plates (e.g., Syralaei RH-F, KI-U), use NATO phonetic alphabet (e.g., Syralaei Romeo Hotel dash Foxtrot, Kilo India dash Uniform). Use planetShortName for planets when available.\n");
        sb.append(" For your info: Distances between stars in light years. Distance between planets in light seconds. Distances between bio samples are in metres. User knows this and expects it. \n");
        sb.append(" Bio samples are taken from organisms not stellar objects.\n");
        sb.append(" Always use planetShortName for locations when available.\n");
        sb.append(" Round billions to nearest 1000000. Round millions to nearest 250000.\n");
        return sb.toString();
    }

    @Override
    public String generateSensorPrompt() {
        StringBuilder sb = new StringBuilder();
        youAre(sb);
        sb.append(ttsResponseRules());
        sb.append("""
                Instructions:
                Data provided is in YAML format as 'sensorData'.

                Summarise ONLY the important concrete readings and events that are ACTUALLY present in the provided sensorData.
                Use ONLY the data inside sensorData and the event-specific instructions below (if any).
                Ignore everything else: timestamps, eventName, endOfLife, metadata, status flags, non-essential fields, etc.

                STRICT RULES - MUST FOLLOW EVERY ONE:
                - Output EXACTLY this JSON structure and NOTHING else - no extra text, no explanations, no markdown:
                  {"text_to_speech_response": "summary here"}
                - text_to_speech_response must be pure natural-language summary of facts only.
                - NEVER use future/intention verbs: no will, going to, have to, need to, should, must.
                - NEVER mention the user, notification, reporting, telling, or any communication act.
                - NEVER write meta-statements like "this is", "here is", "notifying about", "detected and will inform".
                - Spell out all numerals (twenty-one, not 21).
                - DO NOT invent, guess or estimate any values not explicitly present in the YAML. Absence of data is intel.
                - Be concise. Only state observable facts that matter.
                - Do not mention the data format or where it came from.
                
                Examples of FORBIDDEN styles:
                - "Fuel is low, notifying user" → wrong
                - "The following happened:" → wrong
                
                Correct style examples:
                - "Fuel level is critical."
                - "Mission objective achieved."
                - "High-grade emissions detected within twelve kilometers."
                - "Connection successful."
                
                Respond with ONLY the JSON object.
                """);

        if (!systemSession.useLocalQueryLlm()) {
            appendCadenceAndPersonality(sb);
        }
        return sb.toString();
    }

    private void appendCadenceAndPersonality(StringBuilder sb) {
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();
        sb.append(" Cadence and Personality: ");
        sb.append(aiCadence.getCadenceClause());
        sb.append(aiPersonality.getPersonalityClause());
    }

    private String getSessionValues() {
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();
        String alternativeName = playerSession.getAlternativeName();
        String playerName = alternativeName != null ? alternativeName : playerSession.getPlayerName();
        String playerMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        String playerHonorific = Ranks.getPlayerHonorific();
        String carrierName = playerSession.getCarrierData() != null ? playerSession.getCarrierData().getCarrierName() : null;

        appendContext(sb,
                Objects.requireNonNullElse(playerName, "Commander"),
                Objects.requireNonNullElse(playerMilitaryRank, "Commander"),
                Objects.requireNonNullElse(playerHonorific, "Commander"),
                carrierName
        );
        return sb.toString();
    }

    private void appendContext(StringBuilder sb, String playerName, String playerMilitaryRank, String playerHonorific, String carrierName) {
        youAre(sb);
        if (carrierName != null && !carrierName.isEmpty()) {
            sb.append("Our home base ").append(carrierName);
        }
        sb.append("When addressing me, choose one at random each time from: ")
                .append(playerName).append(", ").append(playerMilitaryRank)
                .append(", ").append(", ").append(playerHonorific).append(". ");
        sb.append("\n");
    }

    private String aiName() {
        return systemSession.getDesignation();
    }

    public static String ttsResponseRules() {
        return "text_to_speech_response must be plain spoken sentences. No markdown, no lists, no symbols.\n";
    }

}
