package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.NpcCrewPaidWageEvent;

@SuppressWarnings("unused")
public class NpcCrewPaidWageSubscriber {

    @Subscribe
    public void onNpcCrewPaidWageEvent(NpcCrewPaidWageEvent event) {

    }
}