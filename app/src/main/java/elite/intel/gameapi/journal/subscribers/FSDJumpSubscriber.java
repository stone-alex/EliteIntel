package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.ai.search.edsm.dto.data.DeathsStats;
import elite.intel.ai.search.edsm.dto.data.TrafficStats;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.gameapi.journal.events.FSDJumpEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.AdjustRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static elite.intel.util.GravityCalculator.calculateSurfaceGravity;
import static elite.intel.util.StringUtls.isFuelStarClause;
import static elite.intel.util.StringUtls.subtractString;

@SuppressWarnings("unused")
public class FSDJumpSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();


    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        LocationDto currentLocation = playerSession.getCurrentLocation();
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(event.getStarSystem());
        processEdsmData(systemBodiesDto);
        currentLocation.setGovernment(event.getSystemGovernmentLocalised());
        currentLocation.setAllegiance(event.getSystemAllegiance());
        currentLocation.setSecurity(event.getSystemSecurityLocalised());
        currentLocation.setSecurity(event.getSystemSecurityLocalised());
        currentLocation.setStarName(event.getStarSystem());

        String currentStarSystem = event.getStarSystem();
        currentLocation.setStarName(currentStarSystem);
        currentLocation.setX(event.getStarPos()[0]);
        currentLocation.setY(event.getStarPos()[1]);
        currentLocation.setZ(event.getStarPos()[2]);

        currentLocation.setPopulation(event.getPopulation());
        currentLocation.setPowerplayState(event.getPowerplayState());
        currentLocation.setPowerplayStateControlProgress(event.getPowerplayStateControlProgress());
        currentLocation.setPowerplayStateReinforcement(event.getPowerplayStateReinforcement());
        currentLocation.setPowerplayStateUndermining(event.getPowerplayStateUndermining());
        playerSession.saveCurrentLocation(currentLocation);

        String finalDestination = String.valueOf(playerSession.get(PlayerSession.FINAL_DESTINATION));

        StringBuilder sb = new StringBuilder();
        sb.append(" Hyperspace Jump Successful: ");

        List<NavRouteDto> orderedRoute = playerSession.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();

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
            playerSession.saveCurrentLocation(currentLocation);
        } else if (roueSet) {
            Map<Integer, NavRouteDto> adjustedRoute = AdjustRoute.adjustRoute(orderedRoute);
            playerSession.setNavRoute(adjustedRoute);
            int remainingJump = adjustedRoute.size();
            if (remainingJump > 0) {
                orderedRoute.stream().findFirst().ifPresent(
                        nextStop -> sb
                                .append(" Next stop: ")
                                .append(nextStop.getName())
                                .append(". ")
                                .append("Star Class: ")
                                .append(nextStop.getStarClass())
                                .append(", ")
                                .append(isFuelStarClause(nextStop.getStarClass()))
                );
            }

            sb.append(remainingJump).append(" jumps remaining: ").append(" to ").append(finalDestination).append(".");
        }

        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }

    private void processEdsmData(SystemBodiesDto systemBodiesDto) {
        if(systemBodiesDto == null) return;
        if(systemBodiesDto.getData() == null) return;

        List<BodyData> bodies = systemBodiesDto.getData().getBodies();
        for(BodyData  data : bodies) {
            StellarObjectDto stellarObject = playerSession.getStellarObject(data.getName());
            stellarObject.setAtmosphere(data.getAtmosphereType());
            stellarObject.setBodyId(data.getBodyId());
            stellarObject.setHasRings(data.getRings() != null && !data.getRings().isEmpty());
            stellarObject.setIsTerraformable("Terraformable".equalsIgnoreCase(data.getTerraformingState()));
            stellarObject.setLandable(data.isLandable());
            stellarObject.setMaterials(toMaterials(data.getMaterials()));
            stellarObject.setName(data.getName());
            stellarObject.setMassEM(data.getEarthMasses());
            stellarObject.setRadius(data.getRadius());
            Double surfaceGravity = calculateSurfaceGravity(data.getEarthMasses(), data.getRadius());
            stellarObject.setGravity(surfaceGravity == null ? 0 : surfaceGravity);
            stellarObject.setSurfaceTemperature(data.getSurfaceTemperature());
            stellarObject.setTidalLocked(data.isRotationalPeriodTidallyLocked());
            playerSession.addStellarObject(stellarObject);
        }
    }

    private List<MaterialDto> toMaterials(Map<String, Double> materials) {
        if(materials == null) return new ArrayList<>();
        ArrayList<MaterialDto> materialDtos = new ArrayList<>();
        for(Map.Entry<String, Double> entry : materials.entrySet()) {
            materialDtos.add(new MaterialDto(entry.getKey(), entry.getValue()));
        }
        return materialDtos;
    }
}
