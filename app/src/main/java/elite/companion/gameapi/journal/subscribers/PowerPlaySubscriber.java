package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.PowerplayEvent;
import elite.companion.session.PlayerSession;

import static elite.companion.session.PlayerSession.*;

public class PowerPlaySubscriber {

    public PowerPlaySubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onPowerPlayEvent(PowerplayEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        session.updateSession(PLEDGED_TO_POWER, event.getPower());
        session.updateSession(POWER_RANK, event.getRank());
        session.updateSession(MERITS, event.getMerits());
        session.updateSession(PLEDGED_DURATION, event.getTimePledged());
    }
}
