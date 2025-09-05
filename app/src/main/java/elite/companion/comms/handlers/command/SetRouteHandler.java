package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.util.EventBusManager;
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