package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.DockedEvent;
import elite.intel.session.PlayerSession;

import java.util.List;

public class DockedSubscriber {

    @Subscribe
    public void onDockedEvent(DockedEvent event) {
        StringBuilder sb = new StringBuilder();
        List<String> stationServices = event.getStationServices();
        if (stationServices != null && !stationServices.isEmpty()) {
            sb.append("Services: ");
            for (String service : stationServices) {
                sb.append(service);
                sb.append(", ");
            }
            sb.append(".");
        }

        DockedEvent.LandingPads landingPads = event.getLandingPads();
        if (landingPads != null) {
            sb.append(" Landing Pads:");
            sb.append(" Large: ").append(landingPads.getLarge()).append(", ");
            sb.append(" Medium: ").append(landingPads.getMedium()).append(", ");
            sb.append(" Small: ").append(landingPads.getSmall()).append(".");
        }

        String availableData = LocalServicesData.setLocalServicesData(event.getMarketID());
        if (!availableData.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("Data available for: " + availableData + "."));
        }

        PlayerSession.getInstance().put(PlayerSession.STATION_DATA, sb.toString());
    }
}
