package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.SupercruiseExitEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class SupercruiseExitedSubscriber {

    @Subscribe
    public void onSupercruiseExited(SupercruiseExitEvent event) {
        String starSystem = event.getStarSystem(); // Local Star
        String body = event.getBody(); // Name of the body or station
        String bodyType = event.getBodyType(); // Station Installation or planetary body

        StringBuilder sb = new StringBuilder();
        sb.append("Supercruise exit: ").append(" System: ").append(starSystem).append(", ").append(bodyType).append(": ").append(body);
        //TODO: Store this probably....
        //SystemSession.getInstance().sendToAiAnalysis(sb.toString());
    }

}
