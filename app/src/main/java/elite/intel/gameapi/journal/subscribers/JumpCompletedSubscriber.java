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
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;
import elite.intel.util.AdjustRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static elite.intel.util.GravityCalculator.calculateSurfaceGravity;
import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class JumpCompletedSubscriber {

    PlayerSession playerSession = PlayerSession.getInstance();


    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(event.getStarSystem());
        processEdsmData(systemBodiesDto);

        LocationHistory locationHistory = LocationHistory.getInstance(event.getStarSystem());
        if(locationHistory.getLocations() != null && !locationHistory.getLocations().isEmpty()) {
            playerSession.setLocations(locationHistory.getLocations());
        }

        LocationDto locationDto = locationHistory.getLocations().get(event.getBodyId());

        LocationDto primaryStar =  locationDto == null ? new LocationDto(event.getBodyId()) : locationDto;
        primaryStar.setBodyId(event.getBodyId());
        primaryStar.setStationGovernment(event.getSystemGovernmentLocalised());
        primaryStar.setAllegiance(event.getSystemAllegiance());
        primaryStar.setSecurity(event.getSystemSecurityLocalised());
        primaryStar.setStarName(event.getStarSystem());
        primaryStar.setLocationType(LocationDto.LocationType.PRIMARY_STAR);
        primaryStar.setX(event.getStarPos()[0]);
        primaryStar.setY(event.getStarPos()[1]);
        primaryStar.setZ(event.getStarPos()[2]);
        primaryStar.setPopulation(event.getPopulation());
        primaryStar.setPowerplayState(event.getPowerplayState());
        primaryStar.setPowerplayStateControlProgress(event.getPowerplayStateControlProgress());
        primaryStar.setPowerplayStateReinforcement(event.getPowerplayStateReinforcement());
        primaryStar.setPowerplayStateUndermining(event.getPowerplayStateUndermining());


        String finalDestination = playerSession.getFinalDestination();

        StringBuilder sb = new StringBuilder();
        sb.append(" Hyperspace Jump Successful: ");
        sb.append(" Distance traveled: ").append(event.getJumpDist()).append(" ly. ");

        List<NavRouteDto> orderedRoute = playerSession.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();

        if (finalDestination != null && finalDestination.equalsIgnoreCase(event.getStarSystem())) {
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
            primaryStar.setTrafficDto(trafficDto);
            primaryStar.setDeathsDto(deathsDto);
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

        playerSession.saveLocation(primaryStar);
        if(playerSession.isRouteAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }

    private void processEdsmData(SystemBodiesDto systemBodiesDto) {
        if(systemBodiesDto == null) return;
        if(systemBodiesDto.getData() == null) return;
        List<BodyData> bodies = systemBodiesDto.getData().getBodies();
        if(bodies == null || bodies.isEmpty()) return;

        for(BodyData  data : bodies) {
            LocationDto stellarObject = playerSession.getLocation(data.getId());
            stellarObject.setAtmosphere(data.getAtmosphereType());
            stellarObject.setBodyId(data.getBodyId());
            stellarObject.setHasRings(data.getRings() != null && !data.getRings().isEmpty());
            stellarObject.setTerraformable("Terraformable".equalsIgnoreCase(data.getTerraformingState()));
            stellarObject.setLandable(data.isLandable());
            stellarObject.setMaterials(toMaterials(data.getMaterials()));
            stellarObject.setPlanetName(data.getName());
            stellarObject.setMassEM(data.getEarthMasses());
            stellarObject.setRadius(data.getRadius());
            Double surfaceGravity = calculateSurfaceGravity(data.getEarthMasses(), data.getRadius());
            stellarObject.setGravity(surfaceGravity == null ? 0 : surfaceGravity);
            stellarObject.setSurfaceTemperature(data.getSurfaceTemperature());
            stellarObject.setTidalLocked(data.isRotationalPeriodTidallyLocked());
            stellarObject.setLocationType(determineType(data));
            playerSession.saveLocation(stellarObject);
        }
    }

    private LocationDto.LocationType determineType(BodyData data) {
        String type = data.getType().toLowerCase();
        boolean primaryStar = data.getDistanceToArrival() == 0;
        if(type.contains("star") && primaryStar) return LocationDto.LocationType.PRIMARY_STAR;
        if(type.contains("star") && !primaryStar) return LocationDto.LocationType.STAR;
        if (type.contains("body")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("giant")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("world")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("rogueplanet")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("black hole")) return LocationDto.LocationType.BLACK_HOLE;
        if (type.contains("nebula")) return LocationDto.LocationType.NEBULA;
        return null;
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
