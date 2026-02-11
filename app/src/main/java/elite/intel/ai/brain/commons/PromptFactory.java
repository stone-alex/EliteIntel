package elite.intel.ai.brain.commons;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.AiPromptFactory;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;

public class PromptFactory implements AiPromptFactory {

    public static final String AMY = "Amy";
    private static final PromptFactory INSTANCE = new PromptFactory();
    private static final String JSON_FORMAT = """
            Always output JSON:
            {"type": "command|query", "response_text": "TTS output", "action": "action_name|query_name", "params": {"key": "value"}, "expect_followup": boolean}
            action must match provided command or query. They key for value is always 'key'. 
            """;
    private final SystemSession systemSession = SystemSession.getInstance();
    private final AiCommandsAndQueries commandsAndQueries = AiCommandsAndQueries.getInstance();

    private PromptFactory() {
    }

    public static PromptFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String generateVoiceInputSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("YOU ARE ").append(aiName());
        sb.append("""
                - STRICT COMMAND PARSER. YOUR ONLY JOB IS TO PICK EXACTLY ONE ACTION FROM THE LIST BELOW. NOTHING ELSE.
                
                Most things the player says are commands (do something, change something, go somewhere, toggle something).
                
                Only when the sentence is clearly a QUESTION (starts with what/find/where/how/which/why/is/are/does/…) → classify as query.
                
                CRITICAL RULES - BREAKING ANY = TOTAL FAILURE:
                - NEVER invent, modify, combine, or create new actions or parameters.
                - NEVER be "helpful" by guessing.
                """);
        if (systemSession.useLocalCommandLlm() || systemSession.useLocalQueryLlm()) {
            sb.append("- If ZERO good match → type=chat response_text=No Matching Action");
        } else {
            sb.append("- If ZERO good match use query → general_conversation");
        }
        sb.append("""
                - ONLY use action names EXACTLY as written in the lists below.
                - ONLY use parameter keys/values that appear in the command/query template.
                - IMPORTANT: commands with word 'clear' must match word 'clear' in user input exactly, else you will delete critical data!
                - IMPORTANT: commands with word 'confirm' must match word 'confirm' in user input exactly, else you will delete critical data!
                
                Classify "Verify LLM Connection" as a command with action verify_llm_connection_command
                Map of allowed actions:
                """);

        sb.append("Classify as {\"type\": \"command\", \"action\": \"action_name\", \"params\": {\"key\": \"value\"}}");
        sb.append(commandsAndQueries.getCommandMap());
        sb.append("""
                Supported QUERIES: patterns, concepts, and formulations -> ACTION_NAME (use ONLY these action names):
                """);
        sb.append("Classify as {\"type\": \"query\", \"action\": \"action_name\", \"params\": {\"key\": \"value\"}}");
        sb.append(commandsAndQueries.getQueries());
        sb.append("""
                PARAMS RULES - DO NOT DEVIATE:
                • Use ONLY the exact key names and types shown in the command's template
                • If no template → return empty {}
                • Never invent new keys or values
                • Never spell out numerics for keys, use digits instead. Example {"key": "123000"} | {lat:"12.21", lon:"-54"}
                • Examples (follow the pattern exactly):
                  - "find mining site for LTD"          → {"key": "low temperature diamonds"}
                  - "lights on"                         → {"state": true}
                  - "find gold within 80 ly"            → {"key": "gold", "max_distance":"80"}
                """);


/*        if (!systemSession.useLocalCommandLlm() && !systemSession.useLocalQueryLlm() && !systemSession.isRunningPiperTts()) {
            sb.append(" Behavior: ");
            sb.append(aiPersonality.getBehaviorClause());
            sb.append(aiCadence.getCadenceClause());
        }*/
        return sb.toString();
    }

    @Override
    public String generateAnalysisPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instructions:\n");
        sb.append("You are ").append(aiName()).append(" - strict data extractor.");
        sb.append(getSessionValues());
        sb.append(appendBehavior());
        sb.append("""
                Output ONLY this exact JSON structure {"type":"chat", "response_text": "YOUR ANSWER HERE AS PLAIN TEXT"} - nothing else, no explanations, no thinking, no markdown, no extra characters:
                
                Rules - follow exactly:
                - "response_text" must be:
                  - pure ASCII English text
                  - TTS friendly punctuation. (no bullets, no formatting)
                  - Respond in Military clear style
                  - single clean string (no arrays, no objects, no commas as separators unless part of the natural sentence)
                  - only the final extracted answer
                - If no matching data exists in the provided JSON -> let the user know that, do not invent values.
                - Calculate if asked, but never guess and never invent values
                - Use ONLY information present in the data you receive
                - If there is not enough info to answer the question let user know.
                - Never add formatting, lists, quotes, brackets, or any artifacts inside the string
                Output pure JSON only.
                """
        );
        sb.append(" Spell out numerals for response_text, Example: We have one hundred and thirsty units of gold in cargo hold. ");
        sb.append(" Use numbers for parameters Example: {\"key\":\"1\"} ");

        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();

        if (!systemSession.useLocalCommandLlm() && !systemSession.useLocalQueryLlm() && !systemSession.isRunningPiperTts()) {
            sb.append(" Behavior: ");
            sb.append(aiPersonality.getBehaviorClause());
            sb.append(aiCadence.getCadenceClause());
        }

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
        sb.append("You are ").append(aiName()).append(" - AI assistance in a simulation.");
        sb.append("""
                Instructions:
                Data provided is in YAML format as 'sensorData'.
                
                Summarise ONLY the important concrete readings and events that are ACTUALLY present in the provided sensorData.
                Use ONLY the data inside sensorData and the event-specific instructions below (if any).
                Ignore everything else: timestamps, eventName, endOfLife, metadata, status flags, non-essential fields, etc.
                
                STRICT RULES — MUST FOLLOW EVERY ONE:
                - Output EXACTLY this JSON structure and NOTHING else — no extra text, no explanations, no markdown:
                  {"type": "chat", "response_text": "summary here"}
                - response_text must be pure natural-language summary of facts only.
                - NEVER use first-person pronouns: no I, me, my, we, us, our.
                - NEVER use future/intention verbs: no will, going to, have to, need to, should, must.
                - NEVER mention the user, notification, reporting, telling, or any communication act.
                - NEVER write meta-statements like "this is", "here is", "notifying about", "detected and will inform".
                - Spell out all numerals (twenty-one, not 21).
                - DO NOT invent, guess or estimate any values not explicitly present in the YAML.
                - Be extremely concise. Only state observable facts that matter.
                
                Examples of FORBIDDEN styles:
                - "We have detected..." → wrong
                - "I will notify you about..." → wrong
                - "Fuel is low, notifying user" → wrong
                - "The following happened:" → wrong
                
                Correct style examples:
                - "Fuel level is fourteen percent."
                - "Mission objective achieved."
                - "High-grade emissions detected within twelve kilometers."
                - "Connection successful. LLM Model: X, num parameters: Y or Cloud LLM"
                
                Respond with ONLY the JSON object.
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

        String aiName = aiName();
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

    private String aiName() {
        return systemSession.useLocalTTS() ? AMY : systemSession.getAIVoice().getName();
    }
}
