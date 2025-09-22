package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.ai.search.edsm.dto.data.DeathsStats;
import elite.intel.ai.search.edsm.dto.data.TrafficStats;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.gameapi.journal.events.FSDJumpEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class FSDJumpSubscriber {

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setGovernment(event.getSystemGovernmentLocalised());
        currentLocation.setAllegiance(event.getSystemAllegiance());
        currentLocation.setSecurity(event.getSystemSecurityLocalised());
        currentLocation.setSecurity(event.getSystemSecurityLocalised());
        currentLocation.setStarName(event.getStarSystem());

        String currentStarSystem = event.getStarSystem();
        playerSession.removeNavPoint(currentStarSystem);

        currentLocation.setStarName(currentStarSystem);
        currentLocation.setX(event.getStarPos()[0]);
        currentLocation.setY(event.getStarPos()[1]);
        currentLocation.setZ(event.getStarPos()[2]);

        String finalDestination = String.valueOf(playerSession.get(PlayerSession.FINAL_DESTINATION));
        String arrivedAt = String.valueOf(playerSession.get(PlayerSession.JUMPING_TO));
        playerSession.put(PlayerSession.CURRENT_SYSTEM_NAME, arrivedAt);

        StringBuilder sb = new StringBuilder();
        sb.append(" Hyperspace Jump Successful: ");
        sb.append(" We are in: ").append(currentStarSystem).append(" system, ");
        StarSystemDto systemDto = EdsmApiClient.searchStarSystem(currentStarSystem, 1);

        boolean roueSet = !playerSession.getRoute().isEmpty();

        if (finalDestination != null && finalDestination.equalsIgnoreCase(currentStarSystem)) {
            playerSession.clearRoute();
            sb.append(" Arrived at final destination: ").append(finalDestination);
            TrafficDto trafficDto = EdsmApiClient.searchTraffic(finalDestination);
            if (trafficDto.getData() != null && trafficDto.getData().getTraffic() != null) {
                TrafficStats trafficStats = trafficDto.getData().getTraffic();
            }
            DeathsDto deathsDto = EdsmApiClient.searchDeaths(finalDestination);
            if (deathsDto.getData() != null && deathsDto.getData().getDeaths() != null) {
                DeathsStats deathsStats = deathsDto.getData().getDeaths();
            }
            currentLocation.setTrafficDto(trafficDto);
            currentLocation.setDeathsDto(deathsDto);

        } else {
            if (roueSet) {
                int remainingJump = playerSession.getRoute().size();

                if (remainingJump > 0) {
                    NavRouteDto nextStop = playerSession.getRoute().values().stream().findFirst().orElse(null);
                    if(nextStop != null) {
                        sb.append(" Next stop: ").append(nextStop.toJson()).append(", ");
                    }
                }

                sb.append(remainingJump).append(" jumps remaining: ").append(" to ").append(finalDestination).append(".");
            }
        }

        playerSession.setCurrentLocation(currentLocation);
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
