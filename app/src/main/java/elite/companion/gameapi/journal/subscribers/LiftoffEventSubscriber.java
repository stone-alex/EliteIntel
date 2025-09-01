package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SendToGrokEvent;
import elite.companion.gameapi.journal.events.LiftoffEvent;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        boolean isPlayerControlled = event.isPlayerControlled();
        boolean isOnPlanet = event.isOnPlanet();
        String liftoffType = isPlayerControlled ? "Manual" : "Unmanned";
        String liftoffFromType = isOnPlanet ? "Planet" : "Station";
        String localStarSystem = event.getStarSystem();
        String localBody = event.getBody();

        StringBuilder sb = new StringBuilder();
        sb.append("Liftoff: ");
        sb.append(" Type:");
        sb.append(liftoffType);
        sb.append(", From:");
        sb.append(liftoffFromType);
        sb.append(", ");
        sb.append(localBody);
        sb.append(", in Star system: ");
        sb.append(localStarSystem);

        EventBusManager.publish(new SendToGrokEvent(sb.toString()));
    }
}
