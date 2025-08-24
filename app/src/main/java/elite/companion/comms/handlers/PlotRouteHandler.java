package elite.companion.comms.handlers;

import com.google.gson.JsonObject;
import elite.companion.comms.VoiceGenerator;
import elite.companion.robot.KeyProcessor;
import elite.companion.robot.VoiceCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotRouteHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(PlotRouteHandler.class);
    private final VoiceCommandHandler voiceCommandHandler;

    public PlotRouteHandler(VoiceCommandHandler voiceCommandHandler) {
        this.voiceCommandHandler = voiceCommandHandler;
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        String destination = params.get("destination").getAsString(); //SessionTracker.getInstance().getSessionValue("query_destination", String.class);
        if (destination != null && !destination.isEmpty()) {
            JsonObject plotRouteJson = new JsonObject();
            plotRouteJson.addProperty("type", "command");
            plotRouteJson.addProperty("action", "GalaxyMapOpen");
            plotRouteJson.addProperty("response_text", "Opening galaxy map to plot route to " + destination);
            voiceCommandHandler.handleGrokResponse(plotRouteJson);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted: {}", e.getMessage());
            }

            JsonObject tabToTextJson = new JsonObject();
            tabToTextJson.addProperty("type", "command");
            tabToTextJson.addProperty("action", "UI_Toggle");
            tabToTextJson.addProperty("response_text", "Entering system name");
            voiceCommandHandler.handleGrokResponse(tabToTextJson);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted: {}", e.getMessage());
            }

            simulateTextEntry(destination);

            JsonObject selectJson = new JsonObject();
            selectJson.addProperty("type", "command");
            selectJson.addProperty("action", "UI_Select");
            selectJson.addProperty("response_text", "Selecting " + destination);
            voiceCommandHandler.handleGrokResponse(selectJson);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted: {}", e.getMessage());
            }

            JsonObject plotJson = new JsonObject();
            plotJson.addProperty("type", "command");
            plotJson.addProperty("action", "RoutePlot");
            plotJson.addProperty("response_text", "Route plotted to " + destination);
            voiceCommandHandler.handleGrokResponse(plotJson);

            log.info("Plotted route to: {}", destination);
        } else {
            log.warn("No destination found in session for plot_route");
            VoiceGenerator.getInstance().speak("No destination available to plot route.");
        }
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