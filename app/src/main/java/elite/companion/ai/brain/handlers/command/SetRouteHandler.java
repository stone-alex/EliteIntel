package elite.companion.ai.brain.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.ai.hands.GameCommandHandler;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetRouteHandler extends CustomCommandOperator implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(SetRouteHandler.class);


    public SetRouteHandler(GameCommandHandler gameCommandHandler) {
        super(gameCommandHandler.getMonitor(), gameCommandHandler.getExecutor());
    }

    @Override
    public void handle(JsonObject params, String responseText) {
        String paramKey = CommandActionsCustom.PLOT_ROUTE.getParamKey();
        String destination = params.has(paramKey) ? params.get(paramKey).getAsString() : null;

        if (destination == null || destination.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("No destination set. Please try again."));
        }


    }

}