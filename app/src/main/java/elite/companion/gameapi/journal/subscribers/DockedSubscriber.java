package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.DockedEvent;
import elite.companion.session.PlayerSession;

import java.util.List;

public class DockedSubscriber {

    @Subscribe
    public void onDockedEvent(DockedEvent event) {
        //implement docked event

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

        PlayerSession.getInstance().put(PlayerSession.STATION_DATA, sb.toString());
    }
}
