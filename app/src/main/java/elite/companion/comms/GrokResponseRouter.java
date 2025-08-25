package elite.companion.comms;

import com.google.gson.JsonObject;
import elite.companion.comms.handlers.*;
import elite.companion.robot.VoiceCommandHandler;
import elite.companion.session.PublicSession;
import elite.companion.util.InaraApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static elite.companion.comms.GameCommandMapping.GameCommand.*;

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

        //CUSTOM HANDERS
        commandHandlers.put(INCREASE_ENGINES_POWER.getUserCommand(), new SetPowerToEnginesHandler(voiceCommandHandler));
        commandHandlers.put(INCREASE_SYSTEMS_POWER.getUserCommand(), new SetPowerToSystemsHandler(voiceCommandHandler));
        commandHandlers.put(INCREASE_SHIELDS_POWER.getUserCommand(), new SetPowerToSystemsHandler(voiceCommandHandler));
        commandHandlers.put(INCREASE_WEAPONS_POWER.getUserCommand(), new SetPowerToWeaponsHandler(voiceCommandHandler));

        //GENERIC Handler
        commandHandlers.put(CommandAction.SET_MINING_TARGET.getAction(), new SetMiningTargetHandler());
        commandHandlers.put(CommandAction.PLOT_ROUTE.getAction(), new SetRouteHandler(voiceCommandHandler));
        commandHandlers.put(BACKWARD_KEY.getGameBinding(), new GenericGameController(voiceCommandHandler, BACKWARD_KEY.getGameBinding()));
        commandHandlers.put(AUTO_DOC.getGameBinding(), new GenericGameController(voiceCommandHandler, AUTO_DOC.getGameBinding()));
        commandHandlers.put(CARGO_SCOOP.getGameBinding(), new GenericGameController(voiceCommandHandler, CARGO_SCOOP.getGameBinding()));
        commandHandlers.put(CARGO_SCOOP_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, CARGO_SCOOP_BUGGY.getGameBinding()));
        commandHandlers.put(DEPLOY_HARDPOINT_TOGGLE.getGameBinding(), new GenericGameController(voiceCommandHandler, DEPLOY_HARDPOINT_TOGGLE.getGameBinding()));
        commandHandlers.put(DEPLOY_HEAT_SINK.getGameBinding(), new GenericGameController(voiceCommandHandler, DEPLOY_HEAT_SINK.getGameBinding()));
        commandHandlers.put(DOWN_THRUST_BUTTON.getGameBinding(), new GenericGameController(voiceCommandHandler, DOWN_THRUST_BUTTON.getGameBinding()));
        commandHandlers.put(ENGAGE_SUPERCRUISE.getGameBinding(), new GenericGameController(voiceCommandHandler, ENGAGE_SUPERCRUISE.getGameBinding()));
        commandHandlers.put(EXIT_SETTLEMENT_PLACEMENT_CAMERA.getGameBinding(), new GenericGameController(voiceCommandHandler, EXIT_SETTLEMENT_PLACEMENT_CAMERA.getGameBinding()));
        commandHandlers.put(EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding()));
        commandHandlers.put(EXPLORATION_FSSENTER.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_FSSENTER.getGameBinding()));
        commandHandlers.put(EXPLORATION_FSSQUIT.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_FSSQUIT.getGameBinding()));
        commandHandlers.put(EXPLORATION_SAACHANGE_SCANNED_AREA_VIEW_TOGGLE.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_SAACHANGE_SCANNED_AREA_VIEW_TOGGLE.getGameBinding()));
        commandHandlers.put(EXPLORATION_SAANEXT_GENUS.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_SAANEXT_GENUS.getGameBinding()));
        commandHandlers.put(EXPLORATION_SAAPREVIOUS_GENUS.getGameBinding(), new GenericGameController(voiceCommandHandler, EXPLORATION_SAAPREVIOUS_GENUS.getGameBinding()));
        commandHandlers.put(FOCUS_COMMS_PANEL.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_COMMS_PANEL.getGameBinding()));
        commandHandlers.put(FOCUS_COMMS_PANEL_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_COMMS_PANEL_BUGGY.getGameBinding()));
        commandHandlers.put(FOCUS_COMMS_PANEL_HUMANOID.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_COMMS_PANEL_HUMANOID.getGameBinding()));
        commandHandlers.put(FOCUS_LEFT_PANEL.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_LEFT_PANEL.getGameBinding()));
        commandHandlers.put(FOCUS_LEFT_PANEL_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_LEFT_PANEL_BUGGY.getGameBinding()));
        commandHandlers.put(FOCUS_RADAR_PANEL.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_RADAR_PANEL.getGameBinding()));
        commandHandlers.put(FOCUS_RADAR_PANEL_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_RADAR_PANEL_BUGGY.getGameBinding()));
        commandHandlers.put(FOCUS_RIGHT_PANEL.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_RIGHT_PANEL.getGameBinding()));
        commandHandlers.put(FOCUS_RIGHT_PANEL_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, FOCUS_RIGHT_PANEL_BUGGY.getGameBinding()));
        commandHandlers.put(FORWARD_KEY.getGameBinding(), new GenericGameController(voiceCommandHandler, FORWARD_KEY.getGameBinding()));
        commandHandlers.put(FSTOP_DEC.getGameBinding(), new GenericGameController(voiceCommandHandler, FSTOP_DEC.getGameBinding()));
        commandHandlers.put(FSTOP_INC.getGameBinding(), new GenericGameController(voiceCommandHandler, FSTOP_INC.getGameBinding()));
        commandHandlers.put(GALAXY_MAP.getGameBinding(), new GenericGameController(voiceCommandHandler, GALAXY_MAP.getGameBinding()));
        commandHandlers.put(GALAXY_MAP_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, GALAXY_MAP_BUGGY.getGameBinding()));
        commandHandlers.put(GALAXY_MAP_HOME.getGameBinding(), new GenericGameController(voiceCommandHandler, GALAXY_MAP_HOME.getGameBinding()));
        commandHandlers.put(GALAXY_MAP_HUMANOID.getGameBinding(), new GenericGameController(voiceCommandHandler, GALAXY_MAP_HUMANOID.getGameBinding()));
        commandHandlers.put(GALNET_AUDIO_CLEAR_QUEUE.getGameBinding(), new GenericGameController(voiceCommandHandler, GALNET_AUDIO_CLEAR_QUEUE.getGameBinding()));
        commandHandlers.put(GALNET_AUDIO_PLAY_PAUSE.getGameBinding(), new GenericGameController(voiceCommandHandler, GALNET_AUDIO_PLAY_PAUSE.getGameBinding()));
        commandHandlers.put(GALNET_AUDIO_SKIP_BACKWARD.getGameBinding(), new GenericGameController(voiceCommandHandler, GALNET_AUDIO_SKIP_BACKWARD.getGameBinding()));
        commandHandlers.put(GALNET_AUDIO_SKIP_FORWARD.getGameBinding(), new GenericGameController(voiceCommandHandler, GALNET_AUDIO_SKIP_FORWARD.getGameBinding()));
        commandHandlers.put(HYPER_SUPER_COMBINATION.getGameBinding(), new GenericGameController(voiceCommandHandler, HYPER_SUPER_COMBINATION.getGameBinding()));
        commandHandlers.put(JUMP_TO_HYPERSPACE.getGameBinding(), new GenericGameController(voiceCommandHandler, JUMP_TO_HYPERSPACE.getGameBinding()));
        commandHandlers.put(LANDING_GEAR_TOGGLE.getGameBinding(), new GenericGameController(voiceCommandHandler, LANDING_GEAR_TOGGLE.getGameBinding()));
        commandHandlers.put(OPEN_CODEX_GO_TO_DISCOVERY.getGameBinding(), new GenericGameController(voiceCommandHandler, OPEN_CODEX_GO_TO_DISCOVERY.getGameBinding()));
        commandHandlers.put(OPEN_CODEX_GO_TO_DISCOVERY_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, OPEN_CODEX_GO_TO_DISCOVERY_BUGGY.getGameBinding()));
        commandHandlers.put(PAUSE.getGameBinding(), new GenericGameController(voiceCommandHandler, PAUSE.getGameBinding()));
        commandHandlers.put(PLAYER_HUDMODE_TOGGLE.getGameBinding(), new GenericGameController(voiceCommandHandler, PLAYER_HUDMODE_TOGGLE.getGameBinding()));
        commandHandlers.put(QUICK_COMMS_PANEL_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, QUICK_COMMS_PANEL_BUGGY.getGameBinding()));
        commandHandlers.put(QUICK_COMMS_PANEL_HUMANOID.getGameBinding(), new GenericGameController(voiceCommandHandler, QUICK_COMMS_PANEL_HUMANOID.getGameBinding()));
        commandHandlers.put(RADAR_DECREASE_RANGE.getGameBinding(), new GenericGameController(voiceCommandHandler, RADAR_DECREASE_RANGE.getGameBinding()));
        commandHandlers.put(RADAR_INCREASE_RANGE.getGameBinding(), new GenericGameController(voiceCommandHandler, RADAR_INCREASE_RANGE.getGameBinding()));
        commandHandlers.put(RECALL_DISMISS_SHIP.getGameBinding(), new GenericGameController(voiceCommandHandler, RECALL_DISMISS_SHIP.getGameBinding()));
        commandHandlers.put(REQUEST_REQUEST_DOCK.getGameBinding(), new GenericGameController(voiceCommandHandler, REQUEST_REQUEST_DOCK.getGameBinding()));
        commandHandlers.put(RESET_POWER_DISTRIBUTION.getGameBinding(), new GenericGameController(voiceCommandHandler, RESET_POWER_DISTRIBUTION.getGameBinding()));
        commandHandlers.put(RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding()));
        commandHandlers.put(SET_SPEED100.getGameBinding(), new GenericGameController(voiceCommandHandler, SET_SPEED100.getGameBinding()));
        commandHandlers.put(SET_SPEED25.getGameBinding(), new GenericGameController(voiceCommandHandler, SET_SPEED25.getGameBinding()));
        commandHandlers.put(SET_SPEED50.getGameBinding(), new GenericGameController(voiceCommandHandler, SET_SPEED50.getGameBinding()));
        commandHandlers.put(SET_SPEED75.getGameBinding(), new GenericGameController(voiceCommandHandler, SET_SPEED75.getGameBinding()));
        commandHandlers.put(SET_SPEED_ZERO.getGameBinding(), new GenericGameController(voiceCommandHandler, SET_SPEED_ZERO.getGameBinding()));
        commandHandlers.put(SYSTEM_MAP.getGameBinding(), new GenericGameController(voiceCommandHandler, SYSTEM_MAP.getGameBinding()));
        commandHandlers.put(SYSTEM_MAP_BUGGY.getGameBinding(), new GenericGameController(voiceCommandHandler, SYSTEM_MAP_BUGGY.getGameBinding()));
        commandHandlers.put(SYSTEM_MAP_HUMANOID.getGameBinding(), new GenericGameController(voiceCommandHandler, SYSTEM_MAP_HUMANOID.getGameBinding()));
        commandHandlers.put(TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), new GenericGameController(voiceCommandHandler, TARGET_NEXT_ROUTE_SYSTEM.getGameBinding()));
        commandHandlers.put(TARGET_WINGMAN0.getGameBinding(), new GenericGameController(voiceCommandHandler, TARGET_WINGMAN0.getGameBinding()));
        commandHandlers.put(TARGET_WINGMAN1.getGameBinding(), new GenericGameController(voiceCommandHandler, TARGET_WINGMAN1.getGameBinding()));
        commandHandlers.put(TARGET_WINGMAN2.getGameBinding(), new GenericGameController(voiceCommandHandler, TARGET_WINGMAN2.getGameBinding()));
        commandHandlers.put(UI_DOWN.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_DOWN.getGameBinding()));
        commandHandlers.put(UI_LEFT.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_LEFT.getGameBinding()));
        commandHandlers.put(UI_RIGHT.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_RIGHT.getGameBinding()));
        commandHandlers.put(UIFOCUS.getGameBinding(), new GenericGameController(voiceCommandHandler, UIFOCUS.getGameBinding()));
        commandHandlers.put(UI_SELECT.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_SELECT.getGameBinding()));
        commandHandlers.put(UI_TOGGLE.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_TOGGLE.getGameBinding()));
        commandHandlers.put(UI_UP.getGameBinding(), new GenericGameController(voiceCommandHandler, UI_UP.getGameBinding()));
        commandHandlers.put(UP_THRUST_BUTTON.getGameBinding(), new GenericGameController(voiceCommandHandler, UP_THRUST_BUTTON.getGameBinding()));
        commandHandlers.put(USE_SHIELD_CELL.getGameBinding(), new GenericGameController(voiceCommandHandler, USE_SHIELD_CELL.getGameBinding()));
    }

    public void start() throws Exception {
        voiceCommandHandler.start();
        log.info("Started GrokResponseRouter");
    }

    public void stop() {
        voiceCommandHandler.stop();
        log.info("Stopped GrokResponseRouter");
    }

    public void processGrokResponse(JsonObject jsonResponse) {
        if (jsonResponse == null) {
            log.error("Null Grok response received");
            return;
        }
        try {
            String type = getAsStringOrEmpty(jsonResponse, "type").toLowerCase();
            String responseText = getAsStringOrEmpty(jsonResponse, "response_text");
            String action = getAsStringOrEmpty(jsonResponse, "action");
            JsonObject params = getAsObjectOrEmpty(jsonResponse, "params");

            switch (type) {
                case "command":
                    handleCommand(action, params, responseText, jsonResponse);
                    break;
                case "system_command":
                    handleCommand(action, params, responseText, jsonResponse);
                    break;
                case "query":
                    handleQuery(action, params, responseText);
                    break;
                case "chat":
                    handleChat(responseText);
                    break;
                default:
                    log.warn("Unknown or missing response type: '{}'", type);
                    handleChat(responseText.isEmpty() ? "I'm not sure what you meant. Please try again." : responseText);
            }
        } catch (Exception e) {
            log.error("Failed to process Grok response: {}", e.getMessage(), e);
            handleChat("Error processing response.");
        }
    }

    private static String getAsStringOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return "";
        if (!obj.has(key)) return "";
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return "";
        if (el.isJsonPrimitive()) {
            try {
                return el.getAsString();
            } catch (UnsupportedOperationException ignored) {
                // fallthrough
            }
        }
        // Not a primitive string; log and return empty
        log.debug("Expected string for key '{}' but got {}", key, el);
        return "";
    }

    private static JsonObject getAsObjectOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return new JsonObject();
        if (!obj.has(key)) return new JsonObject();
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonObject();
        if (el.isJsonObject()) return el.getAsJsonObject();
        log.debug("Expected object for key '{}' but got {}", key, el);
        return new JsonObject();
    }

    private void handleCommand(String action, JsonObject params, String responseText, JsonObject jsonResponse) {
        CommandHandler handler = commandHandlers.get(action);
        if (handler != null) {
            handler.handle(params, responseText);
            log.debug("Handled command action: {}", action);
        } else {
            voiceCommandHandler.handleGrokResponse(jsonResponse);
            log.debug("Delegated unhandled command to VoiceCommandHandler: {}", action);
        }
    }

    private void handleQuery(String action, JsonObject params, String responseText) {
        if (action.equals(CommandAction.FIND_NEAREST_MATERIAL_TRADER.getAction())) {
            String currentSystem = PublicSession.getInstance().getSessionValue("current_system", String.class);
            JsonObject result = inaraApiClient.searchNearestMaterialTrader(currentSystem);
            if (result != null && result.has("system_name")) {
                String destination = result.get("system_name").getAsString();
                PublicSession.getInstance().updateSession("query_destination", destination);
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