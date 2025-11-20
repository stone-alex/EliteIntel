package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.HomeSystem;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class SetCurrentStarAsHomeSystem  implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        HomeSystem homeSystem = HomeSystem.getInstance();
        String primaryStar = playerSession.getPrimaryStarName();
        Map<Long, LocationDto> locations = playerSession.getLocations();

        for (Map.Entry<Long, LocationDto> location : locations.entrySet()) {
            if (location.getValue().getStarName().equals(primaryStar)) {
                homeSystem.setHomeSystem(location.getValue());
                EventBusManager.publish(new AiVoxResponseEvent("Home system set to " + primaryStar));
                break;
            }
        }
    }
}
