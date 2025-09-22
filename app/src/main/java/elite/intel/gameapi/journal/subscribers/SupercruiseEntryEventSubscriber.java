package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.SupercruiseEntryEvent;
import elite.intel.session.PlayerSession;

public class SupercruiseEntryEventSubscriber {

    @Subscribe
    public void onSuperCruiseEntryEvent(SupercruiseEntryEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.remove(PlayerSession.STATION_DATA);

        LocalServicesData.clearLocalServicesData();
    }
}
