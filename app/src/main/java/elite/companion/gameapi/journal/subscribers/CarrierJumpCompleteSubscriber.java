package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.CarrierJumpEvent;
import elite.companion.session.SystemSession;

public class CarrierJumpCompleteSubscriber {

    public CarrierJumpCompleteSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        SystemSession.getInstance().updateSession(SystemSession.CURRENT_SYSTEM, "Star System" + starSystem + (event.getBody() == null ? "" : "body " + event.getBody()));

        SystemSession.getInstance().setSensorData("Carrier Jump Complete: " + event.toString());

    }
}
