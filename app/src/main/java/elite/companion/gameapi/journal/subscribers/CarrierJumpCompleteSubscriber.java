package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.CarrierJumpEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        SystemSession.getInstance().put(SystemSession.CURRENT_SYSTEM, "Star System" + starSystem + (event.getBody() == null ? "" : "body " + event.getBody()));

        SystemSession.getInstance().setConsumableData("Carrier Jump Complete: " + event.toString());

    }
}
