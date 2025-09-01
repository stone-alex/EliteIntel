package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SendToGrokEvent;
import elite.companion.gameapi.journal.events.CarrierJumpEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        SystemSession.getInstance().put(SystemSession.CURRENT_SYSTEM, "Star System" + starSystem + (event.getBody() == null ? "" : "body " + event.getBody()));

        EventBusManager.publish(new SendToGrokEvent("Carrier Jump Complete: " + event.toJson()));

    }
}
