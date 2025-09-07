package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.ApproachBodyEvent;
import elite.companion.gameapi.journal.events.dto.MaterialDto;
import elite.companion.gameapi.journal.events.dto.StellarObjectDto;
import elite.companion.session.PlayerSession;

import java.util.List;

public class ApproachBodySubscriber {

    @Subscribe
    public void onApproachBodyEvent(ApproachBodyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        StringBuilder sb = new StringBuilder();

        StellarObjectDto stellarObject = playerSession.getStellarObject(event.getBody());
        if (stellarObject != null) {
            sb.append("Entering orbit for ").append(event.getBody()).append(". ");
            double surfaceGravity = stellarObject.getSurfaceGravity();
            sb.append(" Surface Gravity: ").append(surfaceGravity).append(" g/cm3 ");
            if (surfaceGravity > 1) {
                sb.append(" Gravity Warning!!! ");
            }
            if (!"None".equalsIgnoreCase(stellarObject.getAtmosphere())) {
                sb.append(" Atmosphere: ").append(stellarObject.getAtmosphere());
                sb.append(".");
            }
            List<MaterialDto> materials = stellarObject.getMaterials();
            if (materials != null && !materials.isEmpty()) {
                sb.append(" Materials: ");
                for (MaterialDto material : materials) {
                    sb.append(material.getMaterialName()).append(", ");
                }
            }
            sb.append(".");
            double surfaceTemperature = stellarObject.getSurfaceTemperature();
            sb.append(" Surface Temperature: ").append(surfaceTemperature).append(" C");
            if (stellarObject.isTidalLocked()) sb.append(" The planet is tidally locked. ");
        } else {
            sb.append(" No data available for ").append(event.getBody()).append(".");
            sb.append(" Check gravity and temperature data before landing");
        }

        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
