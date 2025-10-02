package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.ApproachSettlementEvent;

import java.util.List;

public class ApproachSettlementSubscriber {

    @Subscribe
    public void onApproachSettlementEvent(ApproachSettlementEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append("Approaching settlement: ");
        sb.append(event.getName());
        sb.append(", ");

        if ("$government_Engineer;".equalsIgnoreCase(event.getStationGovernment())) {
            sb.append("Engineer: ");
            sb.append(event.getStationFaction().getName());
            sb.append(", ");
        }
        List<String> stationServices = event.getStationServices();
        if (stationServices != null && !stationServices.isEmpty()) {
            sb.append("Services: ");
            for (String services : stationServices) {
                sb.append(services);
                sb.append(", ");
            }
        }

        String availableData = LocalServicesData.setLocalServicesData(event.getMarketID());
        if (!availableData.isEmpty()) sb.append(". More data may be available on request.");

        EventBusManager.publish(new DiscoveryAnnouncementEvent(sb.toString()));
    }
}
