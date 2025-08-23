package elite.companion.comms;

import com.google.gson.JsonObject;
import elite.companion.comms.handlers.CommandHandler;
import elite.companion.comms.handlers.SetMiningTargetHandler;
import elite.companion.robot.VoiceCommandHandler;
import elite.companion.session.SessionTracker;
import elite.companion.util.InaraApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import elite.companion.comms.handlers.*;

import java.util.HashMap;
import java.util.Map;

public class GrokResponseRouter {
    private static final Logger log = LoggerFactory.getLogger(GrokResponseRouter.class);
    private static final GrokResponseRouter INSTANCE = new GrokResponseRouter();
    private final VoiceCommandHandler voiceCommandHandler;
    private final InaraApiClient inaraApiClient;
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public static GrokResponseRouter getInstance() {
        return INSTANCE;
    }

    private GrokResponseRouter() {
        try {
            this.voiceCommandHandler = new VoiceCommandHandler();
            this.inaraApiClient = new InaraApiClient();
            registerCommandHandlers();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GrokResponseRouter", e);
        }
    }

    private void registerCommandHandlers() {
        commandHandlers.put("set_mining_target", new SetMiningTargetHandler());
        commandHandlers.put("plot_route", new PlotRouteHandler(voiceCommandHandler));
        // Add more handlers as needed, e.g., commandHandlers.put("deploy_landing_gear", new DeployLandingGearHandler(voiceCommandHandler));
    }

    public void start() {
        voiceCommandHandler.start();
        log.info("Started GrokResponseRouter");
    }

    public void stop() {
        voiceCommandHandler.stop();
        log.info("Stopped GrokResponseRouter");
    }

    public void processGrokResponse(JsonObject jsonResponse) {
        try {
            String type = jsonResponse.has("type") ? jsonResponse.get("type").getAsString().toLowerCase() : "";
            String responseText = jsonResponse.has("response_text") ? jsonResponse.get("response_text").getAsString() : "";
            String action = jsonResponse.has("action") ? jsonResponse.get("action").getAsString() : "";
            JsonObject params = jsonResponse.has("params") ? jsonResponse.get("params").getAsJsonObject() : new JsonObject();

            switch (type) {
                case "command":
                    handleCommand(action, params, responseText, jsonResponse);
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

    private void handleCommand(String action, JsonObject params, String responseText, JsonObject jsonResponse) {
        CommandHandler handler = commandHandlers.get(action);
        if (handler != null) {
            handler.handle(params, responseText);
            log.debug("Handled command action: {}", action);
        } else {
            // Fallback to VoiceCommandHandler for keyboard-related commands
            voiceCommandHandler.handleGrokResponse(jsonResponse);
            log.debug("Delegated unhandled command to VoiceCommandHandler: {}", action);
        }
    }

    private void handleQuery(String action, JsonObject params, String responseText) {
        // Similar pattern can be applied for queries if they grow
        if (action.equals("find_nearest_material_trader")) {
            String currentSystem = SessionTracker.getInstance().getSessionValue("current_system", String.class);
            JsonObject result = inaraApiClient.searchNearestMaterialTrader(currentSystem);
            if (result != null && result.has("system_name")) {
                String destination = result.get("system_name").getAsString();
                SessionTracker.getInstance().updateSession("query_destination", destination);
                log.info("Found nearest material trader in: {}", destination);
                handleChat(responseText + " Would you like to plot a route there?");
            } else {
                log.warn("No material trader found");
                handleChat("No material trader found nearby.");
            }
        } else {
            log.debug("Unhandled query action: {}", action);
            handleChat(responseText);
        }
    }

    private void handleChat(String responseText) {
        VoiceGenerator.getInstance().speak(responseText);
        log.debug("Sent to VoiceGenerator: {}", responseText);
    }
}