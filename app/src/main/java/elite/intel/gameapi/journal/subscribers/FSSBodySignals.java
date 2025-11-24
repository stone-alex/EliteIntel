package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.StringUtls.subtractString;

public class FSSBodySignals {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onFSSBodySignals(FSSBodySignalsEvent event) {

        boolean containsLife = false;
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        for (FSSBodySignalsEvent.Signal signal : signals) {
            if ("Biological".equalsIgnoreCase(signal.getTypeLocalised())) {
                containsLife = true;
                break;
            }
        }

        if (containsLife) {
            String starName = playerSession.getCurrentLocation().getStarName();
            EventBusManager.publish(
                    new DiscoveryAnnouncementEvent("Life detected in " + subtractString(event.getBodyName(), starName) + "!")
            );
        }
    }
}
