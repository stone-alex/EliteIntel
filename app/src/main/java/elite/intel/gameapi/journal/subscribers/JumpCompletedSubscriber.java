package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.ai.mouth.subscribers.events.RouteAnnouncementEvent;
import elite.intel.db.dao.RouteMonetisationDao.MonetisationTransaction;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.ShipRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.dtos.NavRouteDto;
import elite.intel.gameapi.journal.events.FSDJumpEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.SystemBodiesDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.search.edsm.dto.data.BodyData;
import elite.intel.search.edsm.dto.data.DeathsStats;
import elite.intel.search.edsm.dto.data.TrafficStats;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static elite.intel.util.GravityCalculator.calculateSurfaceGravity;
import static elite.intel.util.StringUtls.isFuelStarClause;

@SuppressWarnings("unused")
public class JumpCompletedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final ShipRouteManager shipRoute = ShipRouteManager.getInstance();
    private final MonetizeRouteManager monetizeRouteManager = MonetizeRouteManager.getInstance();
    private final ReminderManager destinationReminderManager = ReminderManager.getInstance();


    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(event.getStarSystem());
        processEdsmData(systemBodiesDto, event.getSystemAddress());

        boolean isSellerSystem = monetizeRouteManager.isSeller(event.getStarSystem());
        boolean isBuyerSystem = monetizeRouteManager.isBuyer(event.getStarSystem());
        MonetisationTransaction station = monetizeRouteManager.getTransaction();

        LocationDto primaryStar = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyId());
        primaryStar.setBodyId(event.getBodyId());
        primaryStar.setSystemAddress(event.getSystemAddress());
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
        playerSession.setCurrentLocationId(primaryStar.getBodyId(), event.getSystemAddress());
        playerSession.setCurrentPrimaryStarName(primaryStar.getStarName());


        String finalDestination = playerSession.getFinalDestination();

        StringBuilder sb = new StringBuilder();
        List<NavRouteDto> orderedRoute = shipRoute.getOrderedRoute();
        boolean roueSet = !orderedRoute.isEmpty();
        String reminderText = destinationReminderManager.getReminderText();

        if (finalDestination != null && finalDestination.equalsIgnoreCase(event.getStarSystem())) {
            shipRoute.clearRoute();
            if (reminderText != null && !reminderText.isBlank()) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Reminder " + reminderText));
            } else {
                sb.append(" Arrived at final destination: ").append(finalDestination);
            }
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
            if (reminderText.toLowerCase().contains(event.getStarSystem().toLowerCase(Locale.ROOT))) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Reminder " + reminderText));
            }

            sb.append("Arrived at ").append(event.getStarSystem());
            List<NavRouteDto> adjustedRoute = shipRoute.removeLeg(event.getStarSystem());
            int remainingJump = adjustedRoute.size();
            if (remainingJump > 0) {
                adjustedRoute.stream().findFirst().ifPresent(
                        nextStop -> sb
                                .append(". Next Waypoint: ")
                                .append(nextStop.getName())
                                .append(". ")
                                .append(" Star Class: ")
                                .append(nextStop.getStarClass())
                                .append(" ")
                                .append(isFuelStarClause(nextStop.getStarClass()))
                );
                sb.append(". We have ").append(remainingJump).append(" jump");
                if (remainingJump > 1) sb.append("s");
                sb.append(" left to destination.");
            }
        }

        locationManager.save(primaryStar);

        if (playerSession.isRouteAnnouncementOn()) {
            EventBusManager.publish(new RouteAnnouncementEvent(sb.toString()));
        }
        if (isSellerSystem && station != null) {
            EventBusManager.publish(new SensorDataEvent("Head to " + station.getSourceStationName() + " buy " + station.getSourceCommodity(), "Remind User"));
        }

        if (isBuyerSystem && station != null) {
            EventBusManager.publish(new SensorDataEvent("Head to " + station.getDestinationStationName() + " sell " + station.getDestinationCommodity(), "Remind User"));
        }
    }

    private void processEdsmData(SystemBodiesDto systemBodiesDto, long systemAddress) {
        if (systemBodiesDto == null) return;
        if (systemBodiesDto.getData() == null) return;
        List<BodyData> bodies = systemBodiesDto.getData().getBodies();
        if (bodies == null || bodies.isEmpty()) return;

        for (BodyData data : bodies) {
            LocationDto stellarObject = locationManager.findBySystemAddress(systemAddress, data.getBodyId());
            stellarObject.setSystemAddress(systemAddress);
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
            stellarObject.setSurfaceTemperature(data.getSurfaceTemperature()); // Keep Kelvin
            stellarObject.setTidalLocked(data.isRotationalPeriodTidallyLocked());
            stellarObject.setLocationType(LocationDto.determineType(data.getType(), data.getDistanceToArrival() == 0));
            if (data.getDiscovery() != null) {
                stellarObject.setOurDiscovery(data.getDiscovery().getCommander() == null);
                stellarObject.setDiscoveredBy(data.getDiscovery().getCommander());
                stellarObject.setDiscoveredOn(data.getDiscovery().getDate());
            }
            stellarObject.setOrbitalPeriod(data.getOrbitalPeriod());
            stellarObject.setAxialTilt(data.getAxialTilt());
            stellarObject.setRotationPeriod(data.getRotationalPeriod());
            stellarObject.setVolcanism(data.getVolcanismType());
            stellarObject.setPlanetClass(data.getSpectralClass());
            locationManager.save(stellarObject);
        }
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
