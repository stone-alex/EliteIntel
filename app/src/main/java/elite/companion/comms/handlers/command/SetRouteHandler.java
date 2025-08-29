package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.comms.ai.robot.KeyProcessor;
import elite.companion.comms.ai.robot.VoiceCommandHandler;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetRouteHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(SetRouteHandler.class);
    private final VoiceCommandHandler voiceCommandHandler;

    public SetRouteHandler(VoiceCommandHandler voiceCommandHandler) {
        this.voiceCommandHandler = voiceCommandHandler;
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        String destination = params.has(CommandActionsCustom.PLOT_ROUTE.getParamKey()) ? params.get(CommandActionsCustom.PLOT_ROUTE.getParamKey()).getAsString() : null;
        if (destination == null || destination.isEmpty()) {
            // Fallback to SessionTracker if params don't provide destination
            destination = String.valueOf(SystemSession.getInstance().get(SystemSession.QUERY_DESTINATION));
        }

        if (destination != null && !destination.isEmpty()) {
            try {
                // Step 1: Open galaxy map
                JsonObject openMapJson = new JsonObject();
                openMapJson.addProperty("type", "command");
                openMapJson.addProperty("action", CommandActionsGame.GameCommand.GALAXY_MAP.getGameBinding());
                openMapJson.addProperty("response_text", "Opening galaxy map to plot route to " + destination);
                voiceCommandHandler.handleGrokResponse(openMapJson);
                Thread.sleep(500); // Wait for map to open

                // Step 2: Navigate to text field
                JsonObject tabToTextJson = new JsonObject();
                tabToTextJson.addProperty("type", "command");
                tabToTextJson.addProperty("action", CommandActionsGame.GameCommand.UI_TOGGLE.getGameBinding());
                tabToTextJson.addProperty("response_text", "Entering system name");
                voiceCommandHandler.handleGrokResponse(tabToTextJson);
                Thread.sleep(200); // Wait for focus

                // Step 3: Enter destination text
                simulateTextEntry(destination);

                // Step 4: Select system
                JsonObject selectJson = new JsonObject();
                selectJson.addProperty("type", "command");
                selectJson.addProperty("action", CommandActionsGame.GameCommand.UI_SELECT.getGameBinding());
                selectJson.addProperty("response_text", "Selecting " + destination);
                voiceCommandHandler.handleGrokResponse(selectJson);
                Thread.sleep(3000); // Wait for map to zoom in

                // Step 5: Plot route
                JsonObject plotJson = new JsonObject();
                plotJson.addProperty("type", "command");
                plotJson.addProperty("action", CommandActionsGame.GameCommand.UI_SELECT.getGameBinding());
                plotJson.addProperty("action_press_and_hold_delay", "1000"); //<-- instruction to press and hold for 1s key is passed in a line above as action
                plotJson.addProperty("response_text", "Route plotted to " + destination);
                voiceCommandHandler.handleGrokResponse(plotJson);

                log.info("Plotted route to: {}", destination);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted during route plotting: {}", e.getMessage());
                VoiceGenerator.getInstance().speak("Error plotting route.");
            }
        } else {
            log.warn("No destination found for plot_route");
            VoiceGenerator.getInstance().speak("No destination available to plot route.");
        }

        // Speak the provided response text
        VoiceGenerator.getInstance().speak(responseText);
    }

    private void simulateTextEntry(String text) {
        try {
            KeyProcessor keyProcessor = KeyProcessor.getInstance();
            for (char c : text.toUpperCase().toCharArray()) {
                try {
                    int keyCode = KeyProcessor.class.getField("KEY_" + c).getInt(null);
                    keyProcessor.pressKey(keyCode);
                    Thread.sleep(50);
                } catch (NoSuchFieldException e) {
                    log.warn("No key mapping for character: {}", c);
                }
            }
            log.debug("Simulated text entry: {}", text);
        } catch (Exception e) {
            log.error("Failed to simulate text entry: {}", e.getMessage());
            VoiceGenerator.getInstance().speak("Error entering text.");
        }
    }
}