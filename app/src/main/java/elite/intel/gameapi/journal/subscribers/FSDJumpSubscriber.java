package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.api.EdsmApiClient;
import elite.intel.ai.search.api.dto.DeathsDto;
import elite.intel.ai.search.api.dto.StarSystemDto;
import elite.intel.ai.search.api.dto.TrafficDto;
import elite.intel.ai.search.api.dto.data.DeathsStats;
import elite.intel.ai.search.api.dto.data.TrafficStats;
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
        playerSession.addSignal(event);

        String currentStarSystem = event.getStarSystem();
        playerSession.removeNavPoint(currentStarSystem);

        LocationDto currentLocation = playerSession.getCurrentLocation();
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


        if (systemDto.getData() != null) {
            if (systemDto.getData().getInformation().getAllegiance() != null) {
                currentLocation.setAllegiance(systemDto.getData().getInformation().getAllegiance());
                sb.append(" Allegiance: ").append(systemDto.getData().getInformation().getAllegiance());
            }
            if (systemDto.getData().getInformation().getSecurity() != null) {
                sb.append(" Security: ").append(systemDto.getData().getInformation().getSecurity());
                currentLocation.setSecurity(systemDto.getData().getInformation().getSecurity());
            } if ( systemDto.getData().getInformation().getGovernment() != null) {
                currentLocation.setGovernment(systemDto.getData().getInformation().getGovernment());
            }
            sb.append(". ");
        }

        playerSession.setCurrentLocation(currentLocation);

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
            playerSession.addSignal(trafficDto);
            playerSession.addSignal(deathsDto);

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

        //EventBusManager.publish(new AppLogEvent("Processing Event: FSDJumpEvent sending sensor data to AI: " + sb));
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
