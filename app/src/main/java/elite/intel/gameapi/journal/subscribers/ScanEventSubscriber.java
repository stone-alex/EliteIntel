package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.ScanEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.GravityCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static elite.intel.gameapi.journal.events.dto.LocationDto.LocationType.*;
import static elite.intel.util.StringUtls.subtractString;

@SuppressWarnings("unused")
public class ScanEventSubscriber {


    private static final Logger log = LogManager.getLogger(ScanEventSubscriber.class);
    PlayerSession playerSession = PlayerSession.getInstance();
    private static final Set<String> valuablePlanetClasses = Set.of(
            "ammonia world",
            "water world",
            "earthlike body",
            "water giant",
            "gas giant with ammonia-based life",
            "helium gas giant",
            "class v gas giant",
            "class iv gas giant",
            "sudarsky class ii gas giant",
            "gas giant with water-based life"
    );

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        String shortName = subtractString(event.getBodyName(), event.getStarSystem());
        String bodyName = event.getBodyName();

        LocationDto lcoation = getOrMakeLocation(event.getBodyID());
        LocationDto.LocationType locationType = determineLocationType(event);
        lcoation.setLocationType(locationType);

        if(!PRIMARY_STAR.equals(locationType)) {
            lcoation.setX(playerSession.getCurrentLocation().getX());
            lcoation.setY(playerSession.getCurrentLocation().getY());
            lcoation.setZ(playerSession.getCurrentLocation().getZ());
        }


        lcoation.setPlanetName(event.getBodyName());
        lcoation.setBodyId(event.getBodyID());
        lcoation.setPlanetShortName(shortName);


        Double gravity = GravityCalculator.calculateSurfaceGravity(event.getMassEM(), event.getRadius());
        if (gravity != null) lcoation.setGravity(gravity); //DO NOT use event.getSurfaceGravity() as it is not accurate
        lcoation.setMassEM(event.getMassEM());
        lcoation.setRadius(event.getRadius());
        lcoation.setSurfaceTemperature(event.getSurfaceTemperature());
        lcoation.setLandable(event.isLandable());
        lcoation.setPlanetClass(event.getPlanetClass());
        lcoation.setTerraformable("Terraformable".equalsIgnoreCase(event.getTerraformState()));
        lcoation.setTidalLocked(event.isTidalLock());
        lcoation.setAtmosphere(event.getAtmosphereType());
        lcoation.setMaterials(toListOfMaterials(event.getMaterials()));

        List<FSSBodySignalsEvent.Signal> fssSignals = playerSession.getCurrentLocation().getFssSignals();

        if (fssSignals != null && !fssSignals.isEmpty()) {
            int countBioSignals = 0;
            int countGeological = 0;
            for (FSSBodySignalsEvent.Signal signal : fssSignals) {
                if ("Biological".equalsIgnoreCase(signal.getTypeLocalised())) {
                    countBioSignals++;
                }
                if ("Geological".equalsIgnoreCase(signal.getTypeLocalised())) {
                    countGeological++;
                }
            }
            lcoation.setBioSignals(countBioSignals);
            lcoation.setGeoSignals(countGeological);
        }


        List<MaterialDto> materials = new ArrayList<>();
        if (event.getMaterials() != null) {
            for (ScanEvent.Material material : event.getMaterials()) {
                materials.add(new MaterialDto(material.getName(), material.getPercent()));
            }
            lcoation.setMaterials(materials);
        }

        announceIfNewDiscovery(event, lcoation);
        playerSession.saveLocation(lcoation);
        playerSession.setLastScan(lcoation);
    }


    private LocationDto.LocationType determineLocationType(ScanEvent event) {
        boolean isStar = event.getStarType() != null && !event.getStarType().isEmpty() || event.getSurfaceTemperature() > 1000;
        boolean isPrimaryStar = event.getDistanceFromArrivalLS() == 0;
        boolean isBeltCluster = event.getBodyName().contains("Belt Cluster");

        if (isPrimaryStar) {
            return PRIMARY_STAR;
        } else if (isStar) {
            return STAR;
        } else if (isBeltCluster) {
            return BELT_CLUSTER;
        } else {
            return PLANET_OR_MOON;
        }
    }


    private void announceIfNewDiscovery(ScanEvent event, LocationDto location) {
        boolean wasDiscovered = event.isWasDiscovered();
        boolean wasMapped = event.isWasMapped();
        String shortName = subtractString(event.getBodyName(), event.getStarSystem());

        location.setOurDiscovery(!wasDiscovered);
        location.setWeMappedIt(!wasMapped);
        location.setPlanetShortName(shortName);

        boolean isStar = event.getStarType() != null && !event.getStarType().isEmpty() || event.getSurfaceTemperature() > 1000;
        boolean isPrimaryStar = event.getDistanceFromArrivalLS() == 0;
        boolean isBeltCluster = event.getBodyName().contains("Belt Cluster");

        if (!wasDiscovered && PLANET_OR_MOON.equals(location.getLocationType())) {
            if (event.getTerraformState() != null && !event.getTerraformState().isEmpty()) {
                EventBusManager.publish(new DiscoveryAnnouncementEvent("New Terraformable planet: " + shortName + " Details available on request. "));
            } else if (event.getPlanetClass() != null && valuablePlanetClasses.contains(event.getPlanetClass().toLowerCase())) {
                EventBusManager.publish(new DiscoveryAnnouncementEvent("New discovery logged: " + event.getPlanetClass()));
            }
        }

        if (wasDiscovered && !STAR.equals(location.getLocationType())) {
            if (!wasMapped && !BELT_CLUSTER.equals(location.getLocationType())) {
                EventBusManager.publish(new DiscoveryAnnouncementEvent(shortName + " was previously discovered, but not mapped. "));
            } else if (!BELT_CLUSTER.equals(location.getLocationType())) {
                String sensorData = getDetails(event, shortName);
                EventBusManager.publish(new DiscoveryAnnouncementEvent(sensorData));
                log.info(sensorData);
            }
        } else if (!wasDiscovered && PRIMARY_STAR.equals(location.getLocationType())) {

            EventBusManager.publish(new DiscoveryAnnouncementEvent("New star system discovered!"));
        } else if (PRIMARY_STAR.equals(location.getLocationType())) {
            EventBusManager.publish(new DiscoveryAnnouncementEvent("Previously discovered!"));
        }
    }

    private LocationDto getOrMakeLocation(long id) {
        LocationDto location = playerSession.getLocations().get(id);
        return location == null ? new LocationDto(LocationDto.LocationType.UNKNOWN) : location;
    }

    private static String getDetails(ScanEvent event, String shortName) {
        boolean hasMats = event.getMaterials() != null && !event.getMaterials().isEmpty();
        boolean isTerraformable = event.getTerraformState() != null && !event.getTerraformState().isEmpty();
        boolean isLandable = event.isLandable();
        String sensorData = "New discovery: " + shortName + ". "
                + (hasMats ? " Materials detected. data available on request, " : " ")
                + (isTerraformable ? " Terraformable, " : " ")
                + (isLandable ? " landable. " : ". ");
        return sensorData;
    }

    private List<MaterialDto> toListOfMaterials(List<ScanEvent.Material> materials) {
        if (materials == null) return new ArrayList<>();
        ArrayList<MaterialDto> result = new ArrayList<>();
        for (ScanEvent.Material material : materials) {
            result.add(new MaterialDto(material.getName(), material.getPercent()));
        }
        return result;
    }
}
