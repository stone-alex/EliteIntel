package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SellOrganicDataEvent;
import elite.intel.session.PlayerSession;

public class SellOrganicDataSubscriber {

    @Subscribe
    public void onSellOrganicDataEvent(SellOrganicDataEvent event) {
        // not sure what to do with this yet.
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearBioSamples();
        CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
        codexEntryManager.clear();
        if (playerSession.isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent("Bio Data Sold: " + event.toJson()));
        }
    }
}
