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
            sb.append("We are traveling through hyperspace on route to ");
            sb.append(event.getStarSystem());
            sb.append(", ");
            sb.append("Star Class: ");
            sb.append(event.getStarClass());
            sb.append(", ");
            sb.append(isFuelStarClause(event.getStarClass()));
            sb.append(". ");
            if (trafficDto.getData() != null && trafficDto.getData().getTraffic().getTotal() > 0) {
                sb.append(" Traffic data: " + trafficDto.getData().getTraffic().toYaml());
            }
            if (deathsDto.getData() != null && deathsDto.getData().getDeaths().getTotal() > 0) {
                sb.append(" Deaths data: " + deathsDto.getData().getDeaths().toYaml());
            }

            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.clearGenusPaymentAnnounced();
            if (playerSession.isRouteAnnouncementOn()) {
                String instructions = """
                Notify User about the star system we are traveling to.
                    - IMPORTANT: Mention star class and if the star is scoopable for hydrogen fuel or not.
                Example 1: In route to X star class Y, scoopable for fuel.
                Example 2: In route to X star class Y, WARNING! No fuel available.
                
                Data may include traffic and fatalities.
                Traffic total,weekly and daily indicates number of ships traveled through this system. Deaths data indicates number of ships lost in this system.
                Example: Traffic: total X, weekly Y, daily Z. Deaths: total A, weekly B, daily C.
                    - IF no traffic data is available, omit mentioning traffic info.
                    - IF no deaths data is available, omit mentioning fatalities.
                """;
                EventBusManager.publish(new SensorDataEvent(sb.toString(), instructions));
            }
        }
    }
}
