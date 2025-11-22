package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
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
public class ScanEventSubscriber extends BiomeAnalyzer {


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

        LocationDto location = playerSession.getLocation(event.getBodyID(), event.getStarSystem());
        LocationDto.LocationType locationType = determineLocationType(event);

        if(BELT_CLUSTER.equals(locationType) ){
            return; // skip belt clusters.
        }

        location.setLocationType(locationType);
        location.setStarClass(event.getStarType());
        location.setPlanetName(event.getBodyName());
        location.setBodyId(event.getBodyID());
        location.setPlanetShortName(shortName);
        location.setVolcanism(event.getVolcanism());


        Double gravity = GravityCalculator.calculateSurfaceGravity(event.getMassEM(), event.getRadius());
        if (gravity != null) location.setGravity(gravity); //DO NOT use event.getSurfaceGravity() as it is not accurate
        location.setMassEM(event.getMassEM());
        location.setStarName(event.getStarSystem());
        location.setPlanetName(event.getBodyName());
        location.setRadius(event.getRadius());
        location.setSurfaceTemperature(event.getSurfaceTemperature());
        location.setLandable(event.isLandable());
        location.setPlanetClass(event.getPlanetClass());
        location.setTerraformable("Terraformable".equalsIgnoreCase(event.getTerraformState()));
        location.setTidalLocked(event.isTidalLock());
        location.setAtmosphere(event.getAtmosphereType());
        location.setMaterials(toListOfMaterials(event.getMaterials()));
        location.setDistance(event.getDistanceFromArrivalLS());
        location.setOurDiscovery(!event.isWasDiscovered());
        location.setRotationPeriod(event.getRotationPeriod());
        location.setOrbitalPeriod(event.getOrbitalPeriod());
        location.setAxialTilt(event.getAxialTilt());
        location.setWeMappedIt(!event.isWasMapped());
        location.setPlanetShortName(subtractString(event.getBodyName(), event.getStarSystem()));


        List<FSSBodySignalsEvent.Signal> fssSignals = playerSession.getLocation(event.getBodyID(), event.getStarSystem()).getFssSignals();

        int countBioSignals = 0;
        int countGeological = 0;
        if (fssSignals != null && !fssSignals.isEmpty()) {
            for (FSSBodySignalsEvent.Signal signal : fssSignals) {
                if ("Biological".equalsIgnoreCase(signal.getTypeLocalised())) {
                    countBioSignals++;
                }
                if ("Geological".equalsIgnoreCase(signal.getTypeLocalised())) {
                    countGeological++;
                }
            }
            if (location.getBioSignals() < countBioSignals) {
                location.setBioSignals(countBioSignals);
            }
            if( location.getGeoSignals() < countGeological) {
                location.setGeoSignals(countGeological);
            }
        }


        List<MaterialDto> materials = new ArrayList<>();
        if (event.getMaterials() != null) {
            for (ScanEvent.Material material : event.getMaterials()) {
                materials.add(new MaterialDto(material.getName(), material.getPercent()));
            }
            location.setMaterials(materials);
        }

        if (location.getBioSignals() > 0 && playerSession.isDiscoveryAnnouncementOn()) {
            if(!"Detailed".equals(event.getScanType())) {
                analyzeBiome(location);
            }
        } else {
            announceIfNewDiscovery(event, location);
        }

        playerSession.saveLocation(location);
        playerSession.setLastScan(location);

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


        boolean isStar = event.getStarType() != null && !event.getStarType().isEmpty() || event.getSurfaceTemperature() > 1000;
        boolean isPrimaryStar = event.getDistanceFromArrivalLS() == 0;
        boolean isBeltCluster = event.getBodyName().contains("Belt Cluster");

        if (!wasDiscovered && PLANET_OR_MOON.equals(location.getLocationType())) {
            if (event.getTerraformState() != null && !event.getTerraformState().isEmpty()) {
                EventBusManager.publish(new DiscoveryAnnouncementEvent("New Terraformable planet: " + shortName + ". Details available on request. "));
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

            EventBusManager.publish(new DiscoveryAnnouncementEvent("Congrats, you’re the first here!"));
        } else if (PRIMARY_STAR.equals(location.getLocationType())) {
            EventBusManager.publish(new DiscoveryAnnouncementEvent("Previously discovered!"));
        }
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
