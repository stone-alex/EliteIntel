package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.AuxiliaryFilesMonitor;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.ScanEvent;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.GravityCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ScanEventSubscriber {

    private static final Logger log = LogManager.getLogger(ScanEventSubscriber.class);
    private static final Set<String> valuablePlanetClasses = Set.of(
            "ammonia world",
            "water world",
            "earth-like world",
            "water giant",
            "gas giant with ammonia-based life",
            "helium gas giant",
            "class v gas giant",
            "class iv gas giant",
            "class ii gas giant",
            "gas giant with water-based life"
    );

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        // data for questions on last scan
        playerSession.put(PlayerSession.LAST_SCAN, event.toJson());

        boolean wasDiscovered = event.isWasDiscovered();
        boolean wasMapped = event.isWasMapped();

        if ("Detailed".equalsIgnoreCase(event.getScanType())) {

            Double gravity = GravityCalculator.calculateSurfaceGravity(event.getMassEM(), event.getRadius());
            //data for discovery missions - detailed scans
            StellarObjectDto stellarObject = new StellarObjectDto();
            stellarObject.setName(event.getBodyName());
            if(gravity != null) stellarObject.setGravity(gravity); //DO NOT use event.getSurfaceGravity() as it is not accurate
            stellarObject.setMassEM(event.getMassEM());
            stellarObject.setRadius(event.getRadius());
            stellarObject.setSurfaceTemperature(event.getSurfaceTemperature());
            stellarObject.setLandable(event.isLandable());
            stellarObject.setPlanetClass(event.getPlanetClass());
            stellarObject.setIsTerraformable("Terraformable".equalsIgnoreCase(event.getTerraformState()));
            stellarObject.setTidalLocked(event.isTidalLock());
            stellarObject.setAtmosphere(event.getAtmosphereType());

            List<MaterialDto> materials = new ArrayList<>();
            if (event.getMaterials() != null) {
                for (ScanEvent.Material material : event.getMaterials()) {
                    materials.add(new MaterialDto(material.getName(), material.getPercent()));
                }
                stellarObject.setMaterials(materials);
            }
            playerSession.addStellarObject(stellarObject);

            if (!wasDiscovered) {
                //new discovery NOTE: this might be a bit too much. check in game
                if (event.getTerraformState()!= null && !event.getTerraformState().isEmpty()) {
                    EventBusManager.publish(new SensorDataEvent("New Terraformable planet: " + event.getBodyName() + " Details: " + event.toJson()));
                } else if(valuablePlanetClasses.contains(event.getPlanetClass().toLowerCase())){
                    EventBusManager.publish(new SensorDataEvent("New discovery logged: " + event.getPlanetClass()));
                } else {
                    log.info("Skipping Discovery Announcement: " + event.getPlanetClass());
                }
            }


        } else if ("AutoScan".equalsIgnoreCase(event.getScanType())) {

            boolean isStar = event.getDistanceFromArrivalLS() == 0 && event.getSurfaceTemperature() > 2000;
            if (!isStar) {
                boolean isBeltCluster = event.getBodyName().contains("Belt Cluster");
                if (wasDiscovered && !wasMapped && !isBeltCluster) {
                    EventBusManager.publish(new SensorDataEvent(event.getBodyName() + " was previously discovered, but not mapped. "));
                } else if (!wasDiscovered && !isBeltCluster) {
                    boolean hasMats = event.getMaterials() != null && !event.getMaterials().isEmpty();
                    boolean isTerraformable = event.getTerraformState()!= null && !event.getTerraformState().isEmpty();
                    boolean isLandable = event.isLandable();
                    String sensorData = "New discovery: " + event.getBodyName() + ". "
                            + (hasMats ? " materials detected: " : "")
                            + (isTerraformable ? " terraformable: " : event.getTerraformState())
                            + (isLandable ? " landable " : ". ");

                    EventBusManager.publish(new SensorDataEvent(sensorData));
                }
            } else if (!wasDiscovered) {
                EventBusManager.publish(new SensorDataEvent("New star system discovered!"));
            }
        }
    }
}
