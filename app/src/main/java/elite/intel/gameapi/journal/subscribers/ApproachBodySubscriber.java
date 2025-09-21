package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ApproachBodyEvent;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;

import java.util.List;
import java.util.Map;

public class ApproachBodySubscriber {
    private static double formatDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Subscribe
    public void onApproachBodyEvent(ApproachBodyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("Entering orbit for ").append(event.getBody()).append(". ");
        String currentSystem = event.getStarSystem();
        playerSession.put(PlayerSession.LANDED_ON_BODY, event.getBody());
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(currentSystem);

        boolean hasBodiesData = systemBodiesDto.getData() != null
                && systemBodiesDto.getData().getBodies() != null
                && !systemBodiesDto.getData().getBodies().isEmpty();

        if (hasBodiesData) {
            List<BodyData> bodies = systemBodiesDto.getData().getBodies();
            for (BodyData bodyData : bodies) {
                if (bodyData.getName().equalsIgnoreCase(event.getBody())) {
                    double gravity = formatDouble(bodyData.getGravity());
                    sb.append(" Surface Gravity: ").append(gravity).append("G, ");
                    if (gravity > 1) {
                        sb.append(" Gravity Warning!!! ");
                    }
                    int surfaceTemperatureKelvin = bodyData.getSurfaceTemperature();
                    int surfaceTemperatureCelsius = (int) (surfaceTemperatureKelvin - 273.15);
                    sb.append(" Surface Temperature: ").append(surfaceTemperatureKelvin).append(" Kelvin,").append(" or ").append(surfaceTemperatureCelsius).append(" Celsius");
                    if (bodyData.getAtmosphereType() != null && !bodyData.getAtmosphereType().isEmpty()) {
                        sb.append(" Atmosphere: ").append(bodyData.getAtmosphereType());
                        sb.append(". ");
                    }
                    Map<String, Double> materials = bodyData.getMaterials();
                    if (!materials.isEmpty()) {
                        sb.append(" ").append(materials.size()).append(" materials detected. Details available on request. ");
                    }


                }
            }
        } else {
            StellarObjectDto stellarObject = playerSession.getStellarObject(event.getBody());
            if (stellarObject != null) {
                double surfaceGravity = formatDouble(stellarObject.getSurfaceGravity());
                sb.append(" Surface Gravity: ").append(surfaceGravity).append("G ");
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
        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
    }


}
