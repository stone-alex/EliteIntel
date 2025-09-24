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

import static elite.intel.util.StringUtls.subtractString;

@SuppressWarnings("unused")
public class ScanEventSubscriber {


    private static final Logger log = LogManager.getLogger(ScanEventSubscriber.class);
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
        PlayerSession playerSession = PlayerSession.getInstance();
        boolean wasDiscovered = event.isWasDiscovered();
        boolean wasMapped = event.isWasMapped();

        String shortName = subtractString(event.getBodyName(), event.getStarSystem());
        String bodyName = event.getBodyName();

        if ("Detailed".equalsIgnoreCase(event.getScanType())) {

            Double gravity = GravityCalculator.calculateSurfaceGravity(event.getMassEM(), event.getRadius());
            //data for discovery missions - detailed scans
            StellarObjectDto stellarObject = new StellarObjectDto();
            stellarObject.setName(event.getBodyName());
            stellarObject.setShortName(shortName);
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
            playerSession.setLastScan(stellarObject);

            if (!wasDiscovered) {
                //new discovery NOTE: this might be a bit too much. check in game
                if (event.getTerraformState()!= null && !event.getTerraformState().isEmpty()) {
                    EventBusManager.publish(new SensorDataEvent("New Terraformable planet: " + shortName + " Details: " + stellarObject.toJson()));
                } else if(event.getPlanetClass() != null && valuablePlanetClasses.contains(event.getPlanetClass().toLowerCase())){
                    EventBusManager.publish(new SensorDataEvent("New discovery logged: " + event.getPlanetClass()));
                } else {
                    log.info("Skipping Discovery Announcement: " + event.getPlanetClass());
                }
            }


        } else if ("AutoScan".equalsIgnoreCase(event.getScanType())) {

            boolean isStar = event.getDistanceFromArrivalLS() == 0 && event.getSurfaceTemperature() > 2000;
            if (!isStar) {
                boolean isBeltCluster = bodyName.contains("Belt Cluster");
                if (wasDiscovered && !wasMapped && !isBeltCluster) {
                    EventBusManager.publish(new SensorDataEvent(shortName + " was previously discovered, but not mapped. "));
                } else if (!wasDiscovered && !isBeltCluster) {
                    boolean hasMats = event.getMaterials() != null && !event.getMaterials().isEmpty();
                    boolean isTerraformable = event.getTerraformState()!= null && !event.getTerraformState().isEmpty();
                    boolean isLandable = event.isLandable();
                    String sensorData = "New discovery: " + shortName + ". "
                            + (hasMats ? " Materials detected. data available on request, " : " ")
                            + (isTerraformable ? " Terraformable, " : " ")
                            + (isLandable ? " landable. " : ". ");
                    log.info(sensorData);
                    EventBusManager.publish(new SensorDataEvent(sensorData));
                }
            } else if (!wasDiscovered) {
                EventBusManager.publish(new SensorDataEvent("New star system discovered!"));
            } else {
                EventBusManager.publish(new SensorDataEvent("Previously discovered!"));
            }
        }
    }
}
