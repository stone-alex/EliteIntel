package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.SupercruiseExitEvent;
import elite.companion.session.SystemSession;

public class SupercruiseExitedSubscriber {

    public SupercruiseExitedSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onSupercruiseExited(SupercruiseExitEvent event) {
        String starSystem = event.getStarSystem(); // Local Star
        String body = event.getBody(); // Name of the body or station
        String bodyType = event.getBodyType(); // Station Installation or planetary body

        StringBuilder sb = new StringBuilder();
        sb.append("Supercruise exit: Star system: ").append(starSystem);
        sb.append(" at: ").append(bodyType);
        sb.append(" ").append(body);

        SystemSession.getInstance().setSensorData(sb.toString());
    }

}
