package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

public class SetRouteHandler extends CustomCommandOperator implements CommandHandler {
    private static final Logger log = LogManager.getLogger(SetRouteHandler.class);


    public SetRouteHandler(GameHandler gameHandler) {
        super(gameHandler.getMonitor(), gameHandler.getExecutor());
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        EventBusManager.publish(new VoiceProcessEvent("Not implemented yet."));
/*
        String paramKey = CustomCommands.PLOT_ROUTE.getParamKey();
        String destination = params.has(paramKey) ? params.get(paramKey).getAsString() : null;

        if (destination == null || destination.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("No destination set. Please try again."));
        }
*/


    }

}