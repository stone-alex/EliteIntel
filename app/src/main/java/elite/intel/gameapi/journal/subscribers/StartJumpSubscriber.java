package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class StartJumpSubscriber {

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        if ("Hyperspace".equalsIgnoreCase(event.getJumpType())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Traveling through hyperspace on route to ");
            sb.append(event.getStarSystem());
            sb.append(", ");
            sb.append("Star Class: ");
            sb.append(event.getStarClass());
            sb.append(", ");
            sb.append(isFuelStarClause(event.getStarClass()));
            sb.append(". ");
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
            PlayerSession playerSession = PlayerSession.getInstance();
            LocationHistory locationHistory = LocationHistory.getInstance(playerSession.getCurrentLocation().getStarName());
            locationHistory.saveLocations(playerSession.getLocations());
            playerSession.clearLocations();

        }
    }
}
