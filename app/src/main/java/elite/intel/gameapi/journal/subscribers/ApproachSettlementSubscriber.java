package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.ApproachSettlementEvent;
import elite.intel.session.PlayerSession;

import java.util.List;

public class ApproachSettlementSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onApproachSettlementEvent(ApproachSettlementEvent event) {
        Thread.ofVirtual().start(() -> {
            StringBuilder sb = new StringBuilder();

            sb.append("Approaching settlement: ");
            sb.append(event.getName());
            sb.append(", ");

            if ("$government_Engineer;".equalsIgnoreCase(event.getStationGovernment())) {
                sb.append("Engineer: ");
                sb.append(event.getStationFaction().getName());
                sb.append(", ");
            }


            sb.append("Allegiance: ");
            sb.append(event.getStationAllegiance()).append(". ");
            sb.append("Economy: ");
            sb.append(event.getStationEconomy()).append(". ");
            sb.append("Government: ");
            sb.append(event.getStationGovernment()).append(". ");
            if (event.getStationFaction() != null) {
                sb.append("Controlling Faction: ");
                sb.append(event.getStationFaction().getName()).append(". ");
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
            if (!availableData.isEmpty()) sb.append(". More data available on request.");

            if (playerSession.isRouteAnnouncementOn()) {
                String instructions = """
                            Approaching settlement.
                            Provide very brief summary for the settlement data.
                            Do not list every service.
                        """;
                EventBusManager.publish(new SensorDataEvent(sb.toString(), instructions));
            }
        });
    }
}
