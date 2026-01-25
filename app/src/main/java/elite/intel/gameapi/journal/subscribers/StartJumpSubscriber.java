package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class StartJumpSubscriber {

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        if ("Hyperspace".equalsIgnoreCase(event.getJumpType())) {
            TrafficDto trafficDto = EdsmApiClient.searchTraffic(event.getStarSystem());
            DeathsDto deathsDto = EdsmApiClient.searchDeaths(event.getStarSystem());
            StringBuilder sb = new StringBuilder();
            sb.append("Traveling through hyperspace on route to ");
            sb.append(event.getStarSystem());
            sb.append(", ");
            sb.append("Star Class: ");
            sb.append(event.getStarClass());
            sb.append(", ");
            sb.append(isFuelStarClause(event.getStarClass()));
            sb.append(". ");
            if (trafficDto.getData() != null && trafficDto.getData().getTraffic().getTotal() > 0) {
                sb.append("Traffic data: " + trafficDto.toJson());
            }
            if (deathsDto.getData() != null && deathsDto.getData().getDeaths().getTotal() > 0) {
                sb.append(" Deaths data: " + deathsDto.toJson());
            }

            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.clearGenusPaymentAnnounced();
            if (playerSession.isRouteAnnouncementOn()) {
                EventBusManager.publish(new SensorDataEvent(sb.toString(), "Notify User"));
            }
        }
    }
}
