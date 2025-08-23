package elite.companion.comms;

import com.google.gson.JsonObject;
import elite.companion.robot.VoiceCommandHandler;
import elite.companion.session.SessionTracker;
import elite.companion.util.InaraApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrokResponseRouter {
    private static final Logger log = LoggerFactory.getLogger(GrokResponseRouter.class);
    private static final GrokResponseRouter INSTANCE = new GrokResponseRouter();
    private final VoiceCommandHandler voiceCommandHandler;
    private final InaraApiClient inaraApiClient; // For handling queries that require external API calls

    public static GrokResponseRouter getInstance() {
        return INSTANCE;
    }

    private GrokResponseRouter() {
        try {
            this.voiceCommandHandler = new VoiceCommandHandler();
            this.inaraApiClient = new InaraApiClient(); // Initialize INARA client (implement as needed)
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GrokResponseRouter", e);
        }
    }

    public void start() throws Exception {
        voiceCommandHandler.start();
        log.info("Started GrokResponseRouter");
    }

    public void stop() {
        voiceCommandHandler.stop();
        log.info("Stopped GrokResponseRouter");
    }

    public void processGrokResponse(JsonObject json) {
        try {
            String type = json.has("type") ? json.get("type").getAsString().toLowerCase() : "";
            String responseText = json.has("response_text") ? json.get("response_text").getAsString() : "";
            String action = json.has("action") ? json.get("action").getAsString() : "";
            JsonObject params = json.has("params") ? json.get("params").getAsJsonObject() : new JsonObject();

            switch (type) {
                case "command":
                    handleCommand(action, params, responseText);
                    break;
                case "query":
                    handleQuery(action, params, responseText);
                    break;
                case "chat":
                    handleChat(responseText);
                    break;
                default:
                    log.warn("Unknown response type: {}", type);
                    handleChat("I'm not sure what you meant. Please try again.");
            }
        } catch (Exception e) {
            log.error("Failed to process Grok response: {}", e.getMessage());
            handleChat("Error processing response.");
        }
    }

    private void handleCommand(String action, JsonObject params, String responseText) {
        // Update SessionTracker for all commands
        SessionTracker.getInstance().updateSession("action", action);
        SessionTracker.getInstance().updateSession("params", params);
        log.debug("Updated SessionTracker with action: {}, params: {}", action, params);

        // Handle specific command actions
        if (action.equals("set_mining_target")) {
            String target = params.has("target") ? params.get("target").getAsString() : "";
            if (!target.isEmpty()) {
                SessionTracker.getInstance().updateSession("mining_target", target);
                log.info("Set mining target to: {}", target);
            }
        } else if (action.equals("plot_route")) {
            String destination = SessionTracker.getInstance().getSessionValue("query_destination", String.class);
            if (destination != null && !destination.isEmpty()) {
                // Simulate keyboard actions to plot route
                voiceCommandHandler.handleGrokResponse(params); // Delegate to VoiceCommandHandler for keyboard simulation
                log.info("Plotting route to: {}", destination);
            } else {
                log.warn("No destination found in session for plot_route");
                handleChat("No destination available to plot route.");
            }
        } else {
            // Delegate keyboard-related commands to VoiceCommandHandler
            voiceCommandHandler.handleGrokResponse(params);
        }

        handleChat(responseText);
    }

    private void handleQuery(String action, JsonObject params, String responseText) {
        // Example: Handle "find_nearest_material_trader" query
        if (action.equals("find_nearest_material_trader")) {
            // Use INARA API to search for nearest material trader
            String currentSystem = SessionTracker.getInstance().getSessionValue("current_system", String.class); // Assume from journal
            JsonObject result = inaraApiClient.searchNearestMaterialTrader(currentSystem);
            if (result != null) {
                String destination = result.get("system_name").getAsString();
                SessionTracker.getInstance().updateSession("query_destination", destination);
                log.info("Found nearest material trader in: {}", destination);
                handleChat(responseText + " Would you like to plot a route there?");
            } else {
                log.warn("No material trader found");
                handleChat("No material trader found nearby.");
            }
        } else {
            // Handle other queries as needed
            log.debug("Unhandled query action: {}", action);
            handleChat(responseText);
        }
    }

    private void handleChat(String responseText) {
        VoiceGenerator.getInstance().speak(responseText);
        log.debug("Sent to VoiceGenerator: {}", responseText);
    }
}