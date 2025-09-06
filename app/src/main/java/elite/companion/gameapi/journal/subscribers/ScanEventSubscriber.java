package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.ScanEvent;
import elite.companion.gameapi.journal.events.dto.MaterialDto;
import elite.companion.gameapi.journal.events.dto.StellarObjectDto;
import elite.companion.session.PlayerSession;

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
            for (ScanEvent.Material material : event.getMaterials()) {
                materials.add(new MaterialDto(material.getName(), material.getPercent()));
            }
            stellarObject.setMaterials(materials);
            playerSession.addStellarObject(stellarObject);

            if (!wasDiscovered) {
                //new discovery NOTE: this might be a bit too much. check in game
                EventBusManager.publish(new SensorDataEvent("New Discovery Catalogued: " + event.getBodyName() + " Details: " + event.toJson()));
            }

        } else if ("AutoScan".equalsIgnoreCase(event.getScanType())) {

            if (!wasDiscovered) {
                EventBusManager.publish(new SensorDataEvent("New Discovery: " + event.getBodyName()));
            }
            if (wasDiscovered && !wasMapped) {
                EventBusManager.publish(new SensorDataEvent(event.getBodyName() + " was previously discovered, but not mapped."));
            }
        }
    }
}
