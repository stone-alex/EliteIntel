package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class SetCurrentStarAsHomeSystem  implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();

        String primaryStar = playerSession.getPrimaryStar();
        Map<Long, LocationDto> locations = playerSession.getLocations();
        for (Map.Entry<Long, LocationDto> location : locations.entrySet()) {
            if (location.getValue().getStarName().equals(primaryStar)) {
                playerSession.setHomeSystem(location.getValue());
                EventBusManager.publish(new AiVoxResponseEvent("Home system set to " + primaryStar));
                break;
            }
        }
    }
}
