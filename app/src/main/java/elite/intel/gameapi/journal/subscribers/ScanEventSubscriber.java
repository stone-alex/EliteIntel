package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.ScanEvent;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ScanEventSubscriber {

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        // data for questions on last scan
        playerSession.put(PlayerSession.LAST_SCAN, event.toJson());



        boolean wasDiscovered = event.isWasDiscovered();
        boolean wasMapped = event.isWasMapped();

        if ("Detailed".equalsIgnoreCase(event.getScanType())) {

            //data for discovery missions - detailed scans
            StellarObjectDto stellarObject = new StellarObjectDto();
            stellarObject.setName(event.getBodyName());
            stellarObject.setGravity(event.getSurfaceGravity());
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
                if (!event.getTerraformState().isEmpty()) {
                    EventBusManager.publish(new SensorDataEvent("New Terraformable planet: " + event.getBodyName() + " Details: " + event.toJson()));
                }
            }


        } else if ("AutoScan".equalsIgnoreCase(event.getScanType())) {

            boolean isStar = event.getDistanceFromArrivalLS() == 0 && event.getSurfaceTemperature() > 2000;
            if (!isStar) {
                boolean isBeltCluster = event.getBodyName().contains("Belt Cluster");
                if (wasDiscovered && !wasMapped && !isBeltCluster) {
                    EventBusManager.publish(new SensorDataEvent(event.getBodyName() + " was previously discovered, but not mapped. "));
                } else if (!wasDiscovered && !isBeltCluster) {
                    EventBusManager.publish(new SensorDataEvent("New discovery: " + event.getBodyName() + ". "));
                }
            } else if (!wasDiscovered) {
                EventBusManager.publish(new SensorDataEvent("New star system discovered!"));
            }
        }
    }
}
