package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.SystemBodiesDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.search.edsm.dto.data.BodyData;
import elite.intel.search.edsm.dto.data.DeathsStats;
import elite.intel.search.edsm.dto.data.TrafficStats;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.FSDJumpEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.session.PlayerSession;
import elite.intel.db.managers.ShipRouteManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static elite.intel.util.GravityCalculator.calculateSurfaceGravity;
import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class JumpCompletedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();


    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(event.getStarSystem());
        processEdsmData(systemBodiesDto, event.getStarSystem());

        LocationDto primaryStar = LocationManager.getInstance().findPrimaryStar(event.getStarSystem());
        primaryStar.setBodyId(event.getBodyId());
        primaryStar.setStationGovernment(event.getSystemGovernmentLocalised());
        primaryStar.setAllegiance(event.getSystemAllegiance());
        primaryStar.setSecurity(event.getSystemSecurityLocalised());
        primaryStar.setStarName(event.getStarSystem());
        primaryStar.setPlanetName(event.getBody());
        primaryStar.setLocationType(LocationDto.LocationType.PRIMARY_STAR);
        primaryStar.setX(event.getStarPos()[0]);
        primaryStar.setY(event.getStarPos()[1]);
        primaryStar.setZ(event.getStarPos()[2]);
        primaryStar.setPopulation(event.getPopulation());
        primaryStar.setPowerplayState(event.getPowerplayState());
        primaryStar.setPowerplayStateControlProgress(event.getPowerplayStateControlProgress());
        primaryStar.setPowerplayStateReinforcement(event.getPowerplayStateReinforcement());
        primaryStar.setPowerplayStateUndermining(event.getPowerplayStateUndermining());
        playerSession.setCurrentLocationId(primaryStar.getBodyId());
        playerSession.setCurrentPrimaryStarName(primaryStar.getStarName());


        String finalDestination = playerSession.getFinalDestination();

        StringBuilder sb = new StringBuilder();
        sb.append(" Hyperspace Jump Successful: ");
        sb.append(" Distance traveled: ").append(event.getJumpDist()).append(" ly. ");

        List<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();

        if (finalDestination != null && finalDestination.equalsIgnoreCase(event.getStarSystem())) {
            shipRoute.clearRoute();
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
            sb.append("Arrived at: ").append(event.getStarSystem()).append(" star system.");
            List<NavRouteDto> adjustedRoute = shipRoute.removeLeg(event.getStarSystem());
            int remainingJump = adjustedRoute.size();
            if (remainingJump > 0) {
                adjustedRoute.stream().findFirst().ifPresent(
                        nextStop -> sb
                                .append(" Next stop: ")
                                .append(nextStop.getName())
                                .append(". ")
                                .append("Star Class: ")
                                .append(nextStop.getStarClass())
                                .append(", ")
                                .append(isFuelStarClause(nextStop.getStarClass()))
                );
                sb.append(remainingJump).append(" jumps left.");
            }
        }

        playerSession.saveLocation(primaryStar);

        if (playerSession.isRouteAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }

    private void processEdsmData(SystemBodiesDto systemBodiesDto, String starSystem) {
        if (systemBodiesDto == null) return;
        if (systemBodiesDto.getData() == null) return;
        List<BodyData> bodies = systemBodiesDto.getData().getBodies();
        if (bodies == null || bodies.isEmpty()) return;

        for (BodyData data : bodies) {
            LocationDto stellarObject = playerSession.getLocation(data.getId(), starSystem);
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
            stellarObject.setOurDiscovery(data.getDiscovery().getCommander() == null);
            stellarObject.setDiscoveredBy(data.getDiscovery().getCommander());
            stellarObject.setDiscoveredOn(data.getDiscovery().getDate());
            stellarObject.setOrbitalPeriod(data.getOrbitalPeriod());
            stellarObject.setAxialTilt(data.getAxialTilt());
            stellarObject.setRotationPeriod(data.getRotationalPeriod());
            stellarObject.setVolcanism(data.getVolcanismType());
            stellarObject.setPlanetClass(data.getSpectralClass());
            playerSession.saveLocation(stellarObject);
        }
    }

    private LocationDto.LocationType determineType(BodyData data) {
        String type = data.getType().toLowerCase();
        boolean primaryStar = data.getDistanceToArrival() == 0;
        if (type.contains("star") && primaryStar) return LocationDto.LocationType.PRIMARY_STAR;
        if (type.contains("star") && !primaryStar) return LocationDto.LocationType.STAR;
        if (type.contains("body")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("giant")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("world")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("rogueplanet")) return LocationDto.LocationType.PLANET_OR_MOON;
        if (type.contains("black hole")) return LocationDto.LocationType.BLACK_HOLE;
        if (type.contains("nebula")) return LocationDto.LocationType.NEBULA;
        return null;
    }

    private List<MaterialDto> toMaterials(Map<String, Double> materials) {
        if (materials == null) return new ArrayList<>();
        ArrayList<MaterialDto> materialDtos = new ArrayList<>();
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            materialDtos.add(new MaterialDto(entry.getKey(), entry.getValue()));
        }
        return materialDtos;
    }
}
