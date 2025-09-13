package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.NpcCrewPaidWageEvent;

@SuppressWarnings("unused")
public class NpcCrewPaidWageSubscriber {

    @Subscribe
    public void onNpcCrewPaidWageEvent(NpcCrewPaidWageEvent event) {

    }
}

/**
 *
 * Example event
 * <p>
 * {
 * "timestamp": "2025-08-28T05:54:07Z",
 * "event": "NpcCrewPaidWage",
 * "NpcCrewName": "Isidro Pennington",
 * "NpcCrewId": 235144001,
 * "Amount": 1450127
 * }
 *
 */
