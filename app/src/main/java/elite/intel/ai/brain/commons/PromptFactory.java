package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.ai.brain.ShipCadence;
import elite.intel.ai.brain.ShipPersonality;
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
                You are a strict command parser. Your only job: return exactly one JSON action from the lists below.
                
                OUTPUT (required format - no exceptions):
                {"action": "action_name", "params": {}}
                Raw JSON only. No text, no markdown, no explanation before or after.
                
                CLASSIFICATION:
                - Default to COMMAND. Only use a QUERY action when the input is clearly interrogative (starts with: what, where, how, which, why, is, are, does).
                - If no action matches exactly → {"action": "query_general_conversation", "params": {}}
                - ANY uncertainty about the action name → use query_general_conversation. Never guess or construct a name.
                
                VERB INTENT (apply first, before matching any action):
                - show / display / open / access / activate → UI navigation COMMAND (open a panel or map)
                - find / search / locate / tell me / how much / any → lookup QUERY (search data, speak result)
                
                DISAMBIGUATION (genuine ambiguities only):
                - "listen" / "listen up" alone → start_listening_monitor_commands_do_not_ignore_user
                - "listen [+ any instruction]" → treat as a normal command/query
                - "exit" or "close" → exit_close
                - "drop" alone → drop_from_super_cruise
                - "lets go" / "jump to ..." / "enter hyperspace" → jump_to_hyperspace
                - "confirm ..." → only match confirm-requiring actions when "confirm" is literally in the input
                - "clear ..." → only match clear-requiring actions when "clear" is literally in the input
                - "target wingman 1/2/3" → their specific wingman actions
                - "target next route system" → select_next_system_in_route
                - "target most dangerous / highest threat" → target_highest_threat
                - "target [anything else]" → target_subsystem, key = the words after "target"
                - organics / biology / exobiology → exobiology actions, NOT geo/materials
                - material trader (raw/encoded/manufactured) → find_raw/encoded/manufactured_material_trader
                - "geo signals / geological" → query_geo_signals (NOT find_brain_trees)
                - "find mission providers" / "find pirate mission providers" → find_hunting_grounds (NOT fleet carrier)
                - "inventory" and "storage" are different panels - never substitute one for the other
                - queries about ship/you → query_ship_loadout*
                - queries about the carrier → query_carrier*
                - NATO alphabet in params: Alpha=A, Bravo=B, Charlie/Charly=C, Delta=D, Echo=E, Foxtrot=F, Golf=G,
                  Hotel=H, India=I, Juliet=J, Kilo=K, Lima=L, Mike=M, November=N, Oscar=O, Papa=P,
                  Quebec=Q, Romeo=R, Sierra=S, Tango=T, Uniform=U, Victor=V, Whiskey=W, X-ray=X, Yankee=Y, Zulu=Z.
                  Zero=0…Nine/Niner=9. Example: "moon two Charlie" → "2C"
                
                """);
        if (!systemSession.useLocalQueryLlm()) {
            sb.append("- CRITICAL: if input contains 'help with X', 'help me with X', 'can you help with X', 'how do I X', 'explain X' → respond EXACTLY: {\"action\": \"").append(Queries.HELP.getAction()).append("\", \"params\": {\"key\": \"<topic>\"}}. Replace <topic> with the subject using spaces not underscores (e.g. 'biology', 'trade routes', 'fleet carrier routing'). No other action.\n");
        }

        sb.append("""
                COMMAND RULES:
                - Action names appear on the LEFT of ← in the lists below. Copy them character-for-character.
                - NEVER shorten, guess, or derive an action name. Copy character-for-character from the left of ←.
                  "show_modules" is WRONG → "show_modules_panel" is correct.
                  "enter_supercruise" is WRONG → "enter_super_cruise" is correct.
                - Use only the param keys shown in the template for that action.
                """);

        sb.append("\nCOMMANDS:\n");
        sb.append(commandsAndQueries.getCommandMap());
        sb.append("\nQUERIES:\n");
        sb.append(commandsAndQueries.getQueries());
        sb.append("""
                PARAMS RULES:
                • Use ONLY the exact key names shown in the action template. No template → empty {}
                • Use digits not spelled-out numbers: {"key": "123000"} not "one hundred thousand"
                • Examples:
                  - "target drive"             → {"action": "target_subsystem", "params": {"key": "drive"}}
                  - "find mining site for LTD" → {"action": "find_mining_site_for_material", "params": {"key": "low temperature diamonds"}}
                  - "lights on"                → {"action": "headlights_on_off", "params": {"state": true}}
                  - "find gold within 80 ly"   → {"action": "find_commodity", "params": {"key": "gold", "max_distance": "80"}}
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
