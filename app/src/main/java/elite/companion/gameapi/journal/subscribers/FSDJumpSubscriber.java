package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.DeathsDto;
import elite.companion.ai.search.api.dto.StarSystemDto;
import elite.companion.ai.search.api.dto.TrafficDto;
import elite.companion.ai.search.api.dto.data.DeathsStats;
import elite.companion.ai.search.api.dto.data.TrafficStats;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.FSDJumpEvent;
import elite.companion.session.PlayerSession;
import elite.companion.ui.event.AppLogEvent;

@SuppressWarnings("unused")
public class FSDJumpSubscriber {

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_SYSTEM_DATA, event.toJson());

        String currentStarSystem = event.getStarSystem();

        String finalDestination = String.valueOf(playerSession.get(PlayerSession.FINAL_DESTINATION));
        String arrivedAt = String.valueOf(playerSession.get(PlayerSession.JUMPING_TO));
        playerSession.put(PlayerSession.CURRENT_SYSTEM_NAME, arrivedAt);

        StringBuilder sb = new StringBuilder();
        sb.append(" Hyperspace Jump Successful: ");
        sb.append(" We are in: ").append(currentStarSystem).append(" system, ");
        StarSystemDto systemDto = EdsmApiClient.searchStarSystem(currentStarSystem, 1);

        if (systemDto.getData() != null) {
            if (systemDto.getData().getInformation().getAllegiance() != null) {
                sb.append(" Allegiance: ").append(systemDto.getData().getInformation().getAllegiance());
            }
            if (systemDto.getData().getInformation().getSecurity() != null) {
                sb.append(" Security: ").append(systemDto.getData().getInformation().getSecurity());
            }
            sb.append(". ");
        }

        boolean roueSet = !playerSession.getRoute().isEmpty();

        if (finalDestination != null && finalDestination.equalsIgnoreCase(currentStarSystem)) {
            sb.append(" Arrived at final destination: ").append(finalDestination);
            TrafficDto trafficDto = EdsmApiClient.searchTraffic(finalDestination);
            if (trafficDto.getData() != null && trafficDto.getData().getTraffic() != null) {
                TrafficStats trafficStats = trafficDto.getData().getTraffic();
                if (trafficStats.getTotal() > 0) sb.append("Local traffic: ").append(trafficStats);
            }
            DeathsDto deathsDto = EdsmApiClient.searchDeaths(finalDestination);
            if (deathsDto.getData() != null && deathsDto.getData().getDeaths() != null) {
                DeathsStats deathsStats = deathsDto.getData().getDeaths();
                if (deathsStats.getTotal() > 0) sb.append("Local deaths: ").append(deathsStats);
            }

        } else {
            if (roueSet) {
                int remainingJump = playerSession.getRoute().size();

                if (remainingJump > 0) {
                    sb.append(" Next stop: ").append(playerSession.get(playerSession.FSD_TARGET)).append(", ");
                }

                sb.append(remainingJump).append(" jumps remaining: ").append(" to ").append(finalDestination).append(".");
            }
        }

        EventBusManager.publish(new AppLogEvent("Processing Event: FSDJumpEvent sending sensor data to AI: " + sb));
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
