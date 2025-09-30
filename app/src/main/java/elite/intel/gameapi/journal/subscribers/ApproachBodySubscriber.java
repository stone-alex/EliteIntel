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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static elite.intel.util.StringUtls.subtractString;

public class ApproachBodySubscriber {
    private static double formatDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Subscribe
    public void onApproachBodyEvent(ApproachBodyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        double orbitalCruiseEntryAltitude = playerSession.getStatus().getAltitude();

        StringBuilder sb = new StringBuilder();
        sb.append("Entering orbit for ").append(event.getBody()).append(". ");
        String currentSystem = event.getStarSystem();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setOrbitalCruiseEntryAltitude(orbitalCruiseEntryAltitude);

        if(playerSession.getTracking().isEnabled()) return;


        //clear bio scans if we are landing on a different planet within the same system
        if (!currentLocation.getPlanetName().equalsIgnoreCase(event.getBody())){
            currentLocation.setPartialBioSamples(new ArrayList<>());
        }

        currentLocation.setStarName(event.getStarSystem());
        currentLocation.setPlanetName(event.getBody());
        currentLocation.setPlanetShortName(subtractString(event.getBody(), event.getStarSystem()));

        LocationDto stellarObjectDto = playerSession.getStellarObject(event.getBodyID());
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(currentSystem);

        boolean hasBodiesData = systemBodiesDto.getData() != null
                && systemBodiesDto.getData().getBodies() != null
                && !systemBodiesDto.getData().getBodies().isEmpty();

        //try our own scans first
        if (stellarObjectDto != null) {
            extractDataFromStellarObjectScan(stellarObjectDto, currentLocation, sb);
        } else if (hasBodiesData) {
            List<BodyData> bodies = systemBodiesDto.getData().getBodies();
            for (BodyData bodyData : bodies) {
                if (currentLocation.getPlanetName().equalsIgnoreCase(bodyData.getName())) {
                    extractDataFromEdsm(bodyData, currentLocation, sb);
                    break; //we found the planet in EDSM
                }
            }
        } else { // no data available
            sb.append(" No data available for ").append(event.getBody()).append(".");
            sb.append(" Check gravity and temperature data before landing");
        }
        playerSession.saveCurrentLocation(currentLocation);
        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
    }


    private void extractDataFromStellarObjectScan(LocationDto stellarObjectDto, LocationDto currentLocation, StringBuilder sb) {
        double surfaceGravity = formatDouble(stellarObjectDto.getGravity());
        currentLocation.setGravity(surfaceGravity);

        sb.append(" Surface Gravity: ").append(surfaceGravity).append("G. ");
        if (surfaceGravity > 1) {
            sb.append(" Gravity Warning!!! ");
        }
        if (!"None".equalsIgnoreCase(stellarObjectDto.getAtmosphere())) {
            sb.append(" Atmosphere: ").append(stellarObjectDto.getAtmosphere());
            sb.append(". ");
        }
        List<MaterialDto> materials = stellarObjectDto.getMaterials();
        if (materials != null && !materials.isEmpty()) {
            sb.append(" ").append(materials.size()).append(" materials detected. Details available on request. ");
            for (MaterialDto material : materials) {
                currentLocation.addMaterial(new MaterialDto(material.getName(), material.getPercent()));
            }
        }
        sb.append(".");
        double surfaceTemperature = stellarObjectDto.getSurfaceTemperature();
        currentLocation.setSurfaceTemperature(surfaceTemperature);
        sb.append(" Surface Temperature: ").append(surfaceTemperature).append(" K. ");
        if (stellarObjectDto.isTidalLocked()) sb.append(" The planet is tidally locked. ");
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
