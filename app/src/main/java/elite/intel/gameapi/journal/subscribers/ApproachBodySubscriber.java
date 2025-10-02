package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ApproachBodyEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApproachBodySubscriber {

    private static double formatDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Subscribe
    public void onApproachBodyEvent(ApproachBodyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();

        double orbitalCruiseEntryAltitude = status.getStatus().getAltitude();

        StringBuilder sb = new StringBuilder();
        sb.append("Entering orbit for ").append(event.getBody()).append(". ");
        String currentSystem = event.getStarSystem();

        LocationDto location = playerSession.getLocation(event.getBodyID());
        location.setOrbitalCruiseEntryAltitude(orbitalCruiseEntryAltitude);

        if(playerSession.getTracking().isEnabled()) return;


        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(currentSystem);

        boolean weHaveOurOwnData = location.getGravity() > 0;

        if (weHaveOurOwnData) {
            useOurData(location, sb);
        } else {
            //try EDSM data
            boolean edsmHasData = systemBodiesDto.getData() != null
                    && systemBodiesDto.getData().getBodies() != null
                    && !systemBodiesDto.getData().getBodies().isEmpty();

            if (edsmHasData) {
                List<BodyData> bodies = systemBodiesDto.getData().getBodies();
                for (BodyData bodyData : bodies) {
                    if (location.getPlanetName().equalsIgnoreCase(bodyData.getName())) {
                        extractDataFromEdsm(bodyData, location, sb);
                        break; //we found the planet in EDSM
                    }
                }
            } else { // no data available
                sb.append(" No data available for ").append(event.getBody()).append(".");
                sb.append(" Check gravity and temperature data before landing");
            }
        }

        playerSession.saveCurrentLocation(location);
        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
    }


    private void useOurData(LocationDto location, StringBuilder sb) {
        double surfaceGravity = formatDouble(location.getGravity());

        sb.append(" Surface Gravity: ").append(surfaceGravity).append("G. ");
        if (surfaceGravity > 1) {
            sb.append(" Gravity Warning!!! ");
        }
        if (!"None".equalsIgnoreCase(location.getAtmosphere())) {
            sb.append(" Atmosphere: ").append(location.getAtmosphere());
            sb.append(". ");
        }
        List<MaterialDto> materials = location.getMaterials();
        if (materials != null && !materials.isEmpty()) {
            sb.append(" ").append(materials.size()).append(" materials detected. Details available on request. ");
            for (MaterialDto material : materials) {
                location.addMaterial(new MaterialDto(material.getName(), material.getPercent()));
            }
        }
        sb.append(".");
        double surfaceTemperature = location.getSurfaceTemperature();
        location.setSurfaceTemperature(surfaceTemperature);
        sb.append(" Surface Temperature: ").append(surfaceTemperature).append(" K. ");
        if (location.isTidalLocked()) sb.append(" The planet is tidally locked. ");
    }


    private void extractDataFromEdsm(BodyData bodyData, LocationDto currentLocation, StringBuilder sb) {
        double gravity = formatDouble(bodyData.getGravity());
        currentLocation.setGravity(gravity);
        sb.append(" Surface Gravity: ").append(gravity).append("G. ");
        if (gravity > 1) {
            sb.append(" Gravity Warning!!! ");
        }
        int surfaceTemperatureKelvin = bodyData.getSurfaceTemperature();
        currentLocation.setSurfaceTemperature(surfaceTemperatureKelvin);
        sb.append(" Surface Temperature: ").append(surfaceTemperatureKelvin).append(" K.");
        if (bodyData.getAtmosphereType() != null && !bodyData.getAtmosphereType().isEmpty()) {
            sb.append(" Atmosphere: ").append(bodyData.getAtmosphereType());
            sb.append(". ");
        }
        Map<String, Double> materials = bodyData.getMaterials();
        if (!materials.isEmpty()) {
            sb.append(" ").append(materials.size()).append(" materials detected. Details available on request. ");
            for( Map.Entry<String, Double> material : materials.entrySet()) {
                currentLocation.addMaterial(new MaterialDto(material.getKey(), material.getValue()));
            }
        }
        currentLocation.setPlanetData(bodyData);
    }
}
