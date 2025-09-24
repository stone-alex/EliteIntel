package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.AuxiliaryFilesMonitor;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
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
        boolean isStar = event.getStarType() != null && !event.getStarType().isEmpty() || event.getSurfaceTemperature() > 1000;
        boolean isPrimaryStar = event.getDistanceFromArrivalLS() == 0;
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
            stellarObject.setMaterials(toListOfMaterials(event.getMaterials()));
            FSSBodySignalsEvent fssBodySignalsEvent = playerSession.getFssBodySignals().get(event.getBodyID());
            if(fssBodySignalsEvent != null) {
                List<FSSBodySignalsEvent.Signal> signals = fssBodySignalsEvent.getSignals();
                if(signals != null && !signals.isEmpty()) {
                    int countBioSignals = 0;
                    int countGeological = 0;
                    for(FSSBodySignalsEvent.Signal signal : signals){
                        if("Biological".equalsIgnoreCase(signal.getTypeLocalised())) {
                            countBioSignals++;
                        }
                        if("Geological".equalsIgnoreCase(signal.getTypeLocalised())) {
                            countGeological++;
                        }
                    }
                    stellarObject.setNumBioForms(countBioSignals);
                    stellarObject.setGeoSignals(countGeological);
                }
            }

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
                    EventBusManager.publish(new SensorDataEvent("New Terraformable planet: " + shortName + " Details available on request. "));
                } else if(!isStar && event.getPlanetClass() != null && valuablePlanetClasses.contains(event.getPlanetClass().toLowerCase())){
                    EventBusManager.publish(new SensorDataEvent("New discovery logged: " + event.getPlanetClass()));
                } else {
                    log.info("Skipping Discovery Announcement: " + event.getPlanetClass());
                }
            }


        } else if ("AutoScan".equalsIgnoreCase(event.getScanType())) {

            if (!isStar) {
                boolean isBeltCluster = bodyName.contains("Belt Cluster");
                if (wasDiscovered && !wasMapped && !isBeltCluster) {
                    EventBusManager.publish(new SensorDataEvent(shortName + " was previously discovered, but not mapped. "));
                } else if (!wasDiscovered && !isBeltCluster) {
                    String sensorData = getDetails(event, shortName);
                    EventBusManager.publish(new SensorDataEvent(sensorData));
                    log.info(sensorData);
                }
            } else if (isPrimaryStar && !wasDiscovered) {
                EventBusManager.publish(new SensorDataEvent("New star system discovered!"));
            } else if(isPrimaryStar){
                EventBusManager.publish(new SensorDataEvent("Previously discovered!"));
            } else if(!wasDiscovered){
                EventBusManager.publish(new SensorDataEvent("New Discovery!"));
            }
        }
    }

    private static String getDetails(ScanEvent event, String shortName) {
        boolean hasMats = event.getMaterials() != null && !event.getMaterials().isEmpty();
        boolean isTerraformable = event.getTerraformState()!= null && !event.getTerraformState().isEmpty();
        boolean isLandable = event.isLandable();
        String sensorData = "New discovery: " + shortName + ". "
                + (hasMats ? " Materials detected. data available on request, " : " ")
                + (isTerraformable ? " Terraformable, " : " ")
                + (isLandable ? " landable. " : ". ");
        return sensorData;
    }

    private List<MaterialDto> toListOfMaterials(List<ScanEvent.Material> materials) {
        if(materials == null) return new ArrayList<>();
        ArrayList<MaterialDto> result = new ArrayList<>();
        for(ScanEvent.Material material : materials) {
            result.add(new MaterialDto(material.getName(), material.getPercent()));
        }
        return result;
    }
}
