package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;

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
        } else if ("Supercruise".equalsIgnoreCase(event.getJumpType())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Entering supercruise. ");
            sb.append(" ");
            sb.append(" star system: ");
            sb.append(event.getStarSystem());
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }
}
