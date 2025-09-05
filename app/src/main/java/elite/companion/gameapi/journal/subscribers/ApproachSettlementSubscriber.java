package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.ApproachSettlementEvent;
import elite.companion.util.EventBusManager;

public class ApproachSettlementSubscriber {

    @Subscribe
    public void onApproachSettlementEvent(ApproachSettlementEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append("Approaching settlement: ");
        sb.append(event.getName());
        sb.append(", ");

        if("$government_Engineer;".equalsIgnoreCase(event.getStationGovernment())) {
            sb.append("Engineer: ");
            sb.append(event.getStationFaction().getName());
            sb.append(", ");
        }
        for(String services : event.getStationServices()){
            sb.append(services);
            sb.append(", ");
        }

        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
