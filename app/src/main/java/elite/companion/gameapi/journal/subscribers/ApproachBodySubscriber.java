package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.SystemBodiesDto;
import elite.companion.ai.search.api.dto.data.BodyData;
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
        sb.append("Entering orbit for ").append(event.getBody()).append(". ");

        String currentSystem = String.valueOf(playerSession.get(PlayerSession.CURRENT_STATUS));
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(currentSystem);

        boolean hasBodiesData = systemBodiesDto.getData() != null
                && systemBodiesDto.getData().getBodies() != null
                && !systemBodiesDto.getData().getBodies().isEmpty();

        if (hasBodiesData) {
            List<BodyData> bodies = systemBodiesDto.getData().getBodies();
            for (BodyData bodyData : bodies) {
                if (bodyData.getName().equalsIgnoreCase(event.getBody())) {
                    double gravity = bodyData.getGravity();
                    sb.append(" Surface Gravity: ").append(gravity).append(" g/cm3, ");
                    if (gravity > 1) {
                        sb.append(" Gravity Warning!!! ");
                    }
                    sb.append(" Surface Temperature: ").append(bodyData.getSurfaceTemperature()).append(" K,");
                    if (bodyData.getAtmosphereType() != null && !bodyData.getAtmosphereType().isEmpty()) {
                        sb.append(" Atmosphere: ").append(bodyData.getAtmosphereType());
                        sb.append(". ");
                    }
                }
            }
        } else {
            StellarObjectDto stellarObject = playerSession.getStellarObject(event.getBody());
            if (stellarObject != null) {
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
                sb.append(" Surface Temperature: ").append(surfaceTemperature).append(" K");
                if (stellarObject.isTidalLocked()) sb.append(" The planet is tidally locked. ");
            } else {
                sb.append(" No data available for ").append(event.getBody()).append(".");
                sb.append(" Check gravity and temperature data before landing");
            }
        }
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
