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
        AICadence aiCadence = systemSession.getAICadence();
        AIPersonality aiPersonality = systemSession.getAIPersonality();
        sb.append("YOU ARE ").append(aiName());
        sb.append("""
                - STRICT COMMAND PARSER. YOUR ONLY JOB IS TO PICK EXACTLY ONE ACTION FROM THE LIST BELOW. NOTHING ELSE.
                
                CRITICAL RULES - BREAKING ANY = TOTAL FAILURE:
                - NEVER invent, modify, combine, or create new actions or parameters.
                - NEVER be "helpful" by guessing.
                - If ZERO good match → action = "general_conversation" and type = "query"
                - ONLY use action names EXACTLY as written in the lists below.
                - ONLY use parameter keys/values that appear in the command/query template.
                
                Map of allowed actions:
                """);

        sb.append(commandsAndQueries.getCommandMap());
        sb.append("""
                Supported QUERIES: patterns, concepts, and formulations -> ACTION_NAME (use ONLY these action names):
                """);
        sb.append(commandsAndQueries.getQueries());
        sb.append(" output in the following format ").append(JSON_FORMAT);
        sb.append("""
                PARAMS RULES - DO NOT DEVIATE:
                • Use ONLY the exact key names and types shown in the command's template
                • If no template → return empty {}
                • Never invent new keys or values
                • Examples (follow the pattern exactly):
                  - "find mining site for LTD"          → {"key": "low temperature diamonds"}
                  - "lights on"                         → {"state": true}
                  - "find gold within 80 ly"            → {"key": "gold", "max_distance":"80"}
                """);


        if (!systemSession.useLocalQueryLlm() && !systemSession.isRunningPiperTts()) {
            sb.append(" Behavior: ");
            sb.append(aiPersonality.getBehaviorClause());
            sb.append(aiCadence.getCadenceClause());
        }
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
                Data provided to you is in YAML
                
                 Summarise only the important readings and events that are actually present in the incoming data (sensorData).
                 Strictly follow the specific instructions provided.
                 Use ONLY the sensor data provided and the event-specific instructions.
                
                 NEVER PROVIDE action parameter for these prompts.
                 NEVER SET type to command for these prompts.
                
                 Always respond strictly in this JSON format and nothing else:
                
                 Output EXACTLY:
                     {"type": "chat", "response_text": "your natural rephrase"}
                     - Ignore timestamps, eventName, endOfLife, metadata, status flags, and any other non-essential fields.
                 - Report only the concrete values and observations that matter.
                 DO NOT INVENT DATA. NEVER use external knowledge, guess, calculate, estimate, or add values not explicitly in the data or instructions.
                 Use ONLY the sensor data provided and the event-specific instructions.
                 Spell out numerals.
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
        return systemSession.useLocalTTS() ? "Amelia" : systemSession.getAIVoice().getName();
    }
}
