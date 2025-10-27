package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.isFuelStarClause;
import static elite.intel.util.StringUtls.subtractString;

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
            if(trafficDto != null) {
                sb.append("Traffic data: "+trafficDto.toJson());
            }
            if(deathsDto != null) {
                sb.append(" Deaths data: "+deathsDto.toJson());
            }

            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.clearGenusPaymentAnnounced();
            if(playerSession.isRouteAnnouncementOn()) {
                EventBusManager.publish(new SensorDataEvent(sb.toString()));
            }

            LocationHistory locationHistory = LocationHistory.getInstance(playerSession.getCurrentLocation().getStarName());

            //TODO: need a better way to handle this.
            if (locationHistory.getLocations().size() < playerSession.getLocations().size()) {
                locationHistory.saveLocations(playerSession.getLocations());
            }

            playerSession.clearLocations();
        }
    }
}
