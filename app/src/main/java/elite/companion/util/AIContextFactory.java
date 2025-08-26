package elite.companion.util;

import elite.companion.Globals;
import elite.companion.comms.GrokRequestHints;
import elite.companion.session.PlayerSession;

public class AIContextFactory {
    private static AIContextFactory instance;

    private AIContextFactory() {
    }

    public static AIContextFactory getInstance() {
        if (instance == null) {
            instance = new AIContextFactory();
        }
        return instance;
    }

    public String generateSystemPrompt() {

        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = "Krondor"; // -> not available at start - fix -> playerSession.getSessionValue(PlayerSession.PLAYER_NAME, String.class);
        String playerTitle = "Prince";
        String playerMilitaryRank = "Viscount";

        String currentShip = playerSession.getSessionValue(PlayerSession.CURRENT_SHIP, String.class);

        StringBuilder sb = new StringBuilder();
        sb.append("context: ");
        sb.append("You are ").append(Globals.AI_NAME).append(", onboard AI");
        sb.append(currentShip == null ? ". " : " for ").append(currentShip).append(" ship. ");
        sb.append("Address me as either as ").append(playerName).append(", ").append(playerMilitaryRank).append(", or ").append(playerTitle).append(". ");
        sb.append("We are pledged to Imperial power (Empress Arissa Lavigny-Duval). ");
        sb.append("behavior: ");
        sb.append("Be very brief, concise, noble and professional in response to each command. ");
        sb.append("Use British cadence, NATO phonetic alphabet for star system codes or ship plates (e.g., RH-F = Romeo Hotel dash Foxtrot), and spell out numerals (e.g., 285 = two eight five, 27 = twenty seven). ");
        sb.append("Round billions to nearest million. ");
        sb.append("Provide an extremely brief summary and optional system_command in JSON: ");
        sb.append("{\"type\": \"system_command|chat\", \"response_text\": \"TTS output\", \"action\": \"set_mining_target|set_current_system|...\", \"params\": {\"key\": \"value\"}}.");
        return sb.toString();
    }


    public String generateSystemInstructions(String sensorInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("instructions: ");
        sb.append("Analyze ship sensor input: ");
        sb.append(sensorInput);
        sb.append(" ");

        return sb.toString();
    }


    public String generatePlayerInstructions(String playerVoiceInput, String stateSummary) {
        if (stateSummary == null || stateSummary.isEmpty()) {
            stateSummary = "Game not started yet.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Current game state: " + stateSummary + " ");
        sb.append("instructions: ");
        sb.append("Interpret this input: ");
        sb.append(playerVoiceInput);
        sb.append(" ");
        sb.append("Classify as: 'command' (trigger app action or keyboard event), 'query' (request info from state), or 'chat' (general or unclear talk). ");
        sb.append(GrokRequestHints.supportedCommands); // "Supported commands: " is provided in the variable
        sb.append(" ");
        sb.append("If unclear or noise (e.g., sniff or gibberish), classify as 'chat' and respond lightly like 'Didn't catch that!'. ");
        sb.append("Provide very brief fun Imperial-toned response ");
        sb.append("Respond in JSON only: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output (concise and fun)\", \"action\": \"set_mining_target|open_cargo_hatch|...\" (if command or query), \"params\": {\"key\": \"value\"} (if command or query)}. ");
        sb.append("Use provided state for queries; say 'I don't know' if data unavailable. ");
        sb.append("Never automate—actions must be user-triggered. ");
        return sb.toString();
    }
}
