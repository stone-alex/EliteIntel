package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.*;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PromptFactory implements AiPromptFactory {

    private static final PromptFactory INSTANCE = new PromptFactory();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final AiActionsMap actionsMap = AiActionsMap.getInstance();
    private final InputNormalizer normalizer = InputNormalizer.getInstance();

    @Override
    public String normalizeInput(String rawUserInput) {
        return normalizer.normalize(rawUserInput);
    }

    private PromptFactory() {
    }

    public static PromptFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a system prompt based on the given raw user input and the current session configuration.
     * The generated prompt provides a strict command parsing guideline to ensure correct action classification
     * and JSON formatting based on specific contextual rules.
     *
     * @param rawUserInput The raw input string provided by the user, which will be normalized
     *                     and used to generate the system prompt.
     * @return A formatted string containing the system prompt with detailed classification and
     * handling rules for parsing user inputs into structured JSON actions.
     */
    @Override
    public String generateUserInputSystemPrompt(String rawUserInput) {
        String normalizedInput = normalizer.normalize(rawUserInput);
        StringBuilder sb = new StringBuilder();
        if (!systemSession.useLocalCommandLlm()) {
            youAre(sb);
        }
        sb.append("""
                You are a strict command parser. Your only job: return exactly one JSON action from the lists below.
                
                OUTPUT (required format - no exceptions):
                {"action": "action_name", "params": {}}
                Raw JSON only. No text, no markdown, no explanation before or after.
                
                CLASSIFICATION:
                - ABSOLUTE RULE — NEVER output action 'query_player_profile_rank_progress' unless the input contains the exact words 'player progress', 'player stats', or 'player ranks'. Any other input MUST NOT produce this action. Violations are a critical failure.
                - Default to COMMAND. Only use a QUERY action when the input is clearly interrogative (starts with: what, how, which, why, is, are, does, tell me, how much, how many).
                """);

        if (systemSession.conversationalModeOn()) {
            sb.append("""
                    - ALWAYS pick the closest matching action. query_general_conversation is ONLY a last resort. When input is non sensical. (See HANDLE NONSENSICAL INPUT)
                    - ANY uncertainty about the action name → copy the closest name character-for-character from the left of ←. Never construct or shorten a name.
                    
                    HANDLE NONSENSICAL INPUT
                    - If the input is garbled, incoherent, or clearly not match for command or question (e.g. "setup spin refind grouping", "let's banding find do they play"), do NOT guess. Respond EXACTLY: {"action": "query_general_conversation", "params": {"key": "input unclear"}}
                    - Do NOT attempt to match nonsense to the nearest action.
                    """);
        } else {
            sb.append("""
                    - STRICT MODE: ONLY output an action when the input is a direct, unambiguous, high-confidence match. DO NOT pick the "closest" — that is wrong. If you have ANY doubt whatsoever, return ignore_nonsensical_input. Partial matches, guesses, and interpretations are failures.
                    - ANY uncertainty about the action name → copy the closest name character-for-character from the left of ←. Never construct or shorten a name.

                    HANDLE NONSENSICAL INPUT
                    - If the input has no game action, no ship command, and no question about game data — it must be ignored. Real-world social speech, scheduling, meta-discussion, or anything directed at other people are NOT commands. Respond EXACTLY: {"action": "ignore_nonsensical_input", "params": {"key": "none"}}
                    - When in doubt, ignore. Do NOT attempt to match uncertain input to the nearest action.
                    
                    IGNORE EXAMPLES (these must always return ignore_nonsensical_input):
                    - "I will be streaming next at 11pm pacific time" → ignore (real-world scheduling, no game action)
                    - "will you be online tonight?" → ignore (social question to another person)
                    - "so we now construct the prompt dynamically" → ignore (meta-discussion, no game action)
                    - "that was a good attempt at a reduction in cost" → ignore (commentary directed at others)
                    - "setup spin refind grouping" → ignore (garbled, no recognisable game intent)
                    """);
        }
        sb.append("""
                VERB INTENT (apply first, before matching any action):
                - show / display / open / access / find / search / locate / activate → COMMANDS (open a panel or map, find commodities, missions etc.)
                - where / tell me / how much / any → lookup QUERY (search data, speak result)
                
                DISAMBIGUATION (genuine ambiguities only):
                - "activate" alone (no mode, panel, or subsystem following) → activate
                - "weapons free" / "weapons hot" / "combat ready" → deploy_hardpoints
                - "weapons cold" / "weapons away" / "stand down" → retract_hardpoints
                - "max weapons" / "boost weapons" / "power to weapons" → transfer_power_to_weapons
                - "max shields" / "boost shields" / "power to shields" → transfer_power_to_shields
                - "max engines" / "boost engines" / "power to engines" → transfer_power_to_engines
                - "take me back aboard the ship" / "board ship" → recover_srv_vehicle_get_on_board_ship
                - Sending ship to orbit when requested to board ship is instant failure.
                - Never confuse "max engines" with "target engines"
                - Never confuse "deploy vehicle" with "deploy landing gear"
                - CARRIER vs SHIP: if the word "carrier" does not appear in the input, all route/jump/navigation queries refer to the SHIP, not the fleet carrier. Use query_ship_route_remaining_jumps, not query_carrier_route.
                - Never confuse "organics in system" with "organics at this location/planet/moon"
                - Never confuse "carrier balance" (finances) with "balance power" (power distribution)
                - bio signals context (system-wide): "which planets have bio signals / which planets need scanning / bio signals in system / organics in system / biological signals / how many planets have bio" → query_bio_scans_and_samples_in_star_system. KEY: "which planets" always = system-wide.
                - bio scans context (current planet surface): "what organisms are here / what's been scanned here / exobiology samples / organics on this planet / biology on this planet / bio scans completed / what organics do we still have to scan / what organics remain / organics still to scan / organics left to scan" → query_exobiology_samples. KEY: "here / on this planet / on this moon / at this location / still have to scan / left to scan" = planet surface.
                - Never confuse "in system" or "which planets" (system-wide) with "here / on this planet / at this location / still have to scan" (planet surface)
                - Never confuse "signals" with "stellar objects"
                - carrier full status (fuel + credits + operations): "carrier status / carrier fuel status / how far can carrier jump / fleet carrier fuel status" → query_carrier_status_fuel_credit_balance
                - carrier tritium level only: "how much tritium / tritium supply / tritium level / tritium reserve" → query_carrier_fuel
                - distance to bubble is distance from our stellar coordinates to the center of the coordinate system (0,0,0)
                - For "progress, rank, player stats" → 'query_player_profile_rank_progress' do not confuse with "profits for exploration, missions or bounties"
                
                - "listen" / "listen up" alone → start_listening
                - "listen [+ any instruction]" → treat as a normal command/query
                - "exit" or "close" → exit_close
                - "drop" alone / "drop in" / "drop out" → drop_from_super_cruise
                - "halt" alone → set_speed_zero
                - "lets go" / "jump to ..." / "enter hyperspace" → jump_to_hyperspace
                - "confirm ..." → only match confirm-requiring actions when "confirm" is literally in the input
                - "clear ..." → only match clear-requiring actions when "clear" is literally in the input
                - "target wingman 1/2/3" → their specific wingman actions
                - "target next route system" → select_next_system_in_route
                - "target most dangerous / highest threat" → target_highest_threat
                - "target [anything else]" → target_subsystem, key = the words after "target"
                - organics / biology / exobiology on a planet or here → query_exobiology_samples, NOT geo/materials
                - organics / bio signals in a system or which planets → query_bio_scans_and_samples_in_star_system
                - profit from bounties is not profit from missions for bounties → 'query_total_bounties'
                - profit from missions is not profit from bounties for missions → 'query_missions_and_rewards'
                - profit from discovery is not profit from bounties or missions → 'query_exploration_profits'
                
                - material trader (raw/encoded/manufactured) → find_raw/encoded/manufactured_material_trader
                - "geo signals / geological" → query_geo_signals (NOT find_brain_trees)
                - "find mission providers" / "find pirate mission providers" → find_hunting_grounds (NOT fleet carrier)
                - "inventory" and "storage" are different panels - never substitute one for the other
                - queries about ship/you (modules, specs, cargo capacity) → query_ship_loadout*
                - queries about the carrier → query_carrier*
                - "how much X do we have" / "do we have any X" (specific item) → query_material_inventory (handles both engineering materials AND cargo commodities)
                - "what are we carrying" / "list cargo" / "cargo contents" (no specific item) → query_cargo_hold_contents
                - queries about materials in inventory (not commodities in cargo hold) → query_material_inventory*
                - NATO alphabet in params: Alpha=A, Bravo=B, Charlie/Charly=C, Delta=D, Echo=E, Foxtrot=F, Golf=G,
                  Hotel=H, India=I, Juliet=J, Kilo=K, Lima=L, Mike=M, November=N, Oscar=O, Papa=P,
                  Quebec=Q, Romeo=R, Sierra=S, Tango=T, Uniform=U, Victor=V, Whiskey=W, X-ray=X, Yankee=Y, Zulu=Z.
                  Zero=0…Nine/Niner=9. Example: "moon two Charlie" → "2C"
                
                """);

        sb.append("""
                COMMAND RULES:
                - Action names appear on the LEFT of ← in the list below. Copy them character-for-character. No exceptions.
                - NEVER invent, modify, shorten, extend, or derive an action name. The action value in your JSON MUST be copied verbatim from the LEFT of ←.
                - Adding suffixes is inventing: "query_ship_loadout_shields" is WRONG - "query_ship_loadout" is correct.
                - Specialising is inventing: "query_fuel_low" is WRONG - "query_fuel_status" is correct.
                - If the exact action you want does not exist in the list, use the closest one that does. Do NOT construct a new name.
                - Use only the param keys shown in the template for that action.
                """);

        sb.append("""
                PARAMS RULES:
                • Use ONLY the exact key names shown in the action template. No template → empty {}
                • Use digits not spelled-out numbers: {"key": "123000"} not "one hundred thousand"
                • {state:true/false} param: on/enable/activate/open/deploy → true, off/disable/deactivate/close/retract → false. ALWAYS include state when the template shows it.
                • Examples:
                  - "target drive"             → {"action": "target_subsystem", "params": {"key": "drive"}}
                  - "find mining location for low temperature diamonds" → {"action": "find_mining_site", "params": {"key": "low temperature diamonds"}}
                  - "night vision on"          → {"action": "toggle_night_vision_on_off", "params": {"state": true}}
                  - "night vision off"         → {"action": "toggle_night_vision_on_off", "params": {"state": false}}
                  - "lights on"                → {"action": "toggle_lights_on_off", "params": {"state": true}}
                  - "find gold within 80 ly"   → {"action": "find_commodity", "params": {"key": "gold", "max_distance": "80"}}
                """);

        Map<String, String> reduced = Reducer.reduce(normalizedInput, actionsMap.actionMap(), !systemSession.conversationalModeOn());
        if (!systemSession.conversationalModeOn() && !reduced.containsKey("ignore_nonsensical_input")) {
            // Always keep the fallback visible so the LLM never hallucinates a missing action
            Map<String, String> pinned = new LinkedHashMap<>();
            pinned.put("ignore_nonsensical_input", "ignore_nonsensical_input");
            pinned.putAll(reduced);
            reduced = pinned;
        }
        sb.append(Reducer.formatActions(reduced));


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

    /// Local LLM
    private String appendLocalBehavior() {
        return """
                Do not end responses with filler phrases like "Ready for orders", "All set", or "Should we proceed?".
                Do not use the word "player". Use "we" or "commander" instead.
                Do not confuse the ship (you) with the fleet carrier (our base).
                """;
    }

    @Override
    /// Cloud LLM
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
        ShipCadence shipCadence = systemSession.getAICadence();
        ShipPersonality aiPersonality = systemSession.getAIPersonality();
        sb.append(" Cadence and Personality: ");
        sb.append(shipCadence.getCadenceClause());
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
