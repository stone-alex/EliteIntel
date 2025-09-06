package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.LiftoffEvent;

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
        sb.append(".");
        if (isPlayerControlled && isOnPlanet) sb.append(" Retract landing gear ");

        EventBusManager.publish(new SensorDataEvent(sb.toString()));

    }
}
