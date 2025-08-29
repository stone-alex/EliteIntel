package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.PowerplayEvent;
import elite.companion.session.PlayerSession;

import static elite.companion.session.PlayerSession.*;

@SuppressWarnings("unused")
public class PowerPlaySubscriber {

    @Subscribe
    public void onPowerPlayEvent(PowerplayEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        session.put(PLEDGED_TO_POWER, event.getPower());
        session.put(POWER_RANK, event.getRank());
        session.put(MERITS, event.getMerits());
        session.put(PLEDGED_DURATION, event.getTimePledged());
    }
}
