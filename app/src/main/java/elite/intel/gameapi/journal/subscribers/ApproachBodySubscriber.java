package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.ApproachBodyEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.SystemBodiesDto;
import elite.intel.search.edsm.dto.data.BodyData;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.List;
import java.util.Map;

public class ApproachBodySubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    private static double formatDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Subscribe
    public void onApproachBodyEvent(ApproachBodyEvent event) {
        Status status = Status.getInstance();
        double orbitalCruiseEntryAltitude = status.getStatus().getAltitude();
        StringBuilder sb = new StringBuilder();
        LocationDto location = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyID());

        if (location.getPlanetName() == null || location.getPlanetName().isEmpty()) {
            location.setPlanetName(event.getBody());
            location.setPlanetShortName(event.getBody());
        }


        String locationType = location.getLocationType() == null ? "" : location.getLocationType().name();
        sb.append("Entering orbit for ").append(locationType).append(" ").append(location.getPlanetName()).append(". ");

        String currentSystem = event.getStarSystem();

        location.setOrbitalCruiseEntryAltitude(orbitalCruiseEntryAltitude);
        playerSession.setCurrentLocationId(event.getBodyID());
        playerSession.setCurrentPrimaryStarName(event.getStarSystem());

        if (playerSession.getTracking().isEnabled()) return;


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
                List<BodyData> edsmData = systemBodiesDto.getData().getBodies();
                //we found the planet in EDSM
                edsmData.stream().filter(edsmDataItem ->
                        location.getPlanetName().equalsIgnoreCase(edsmDataItem.getName())).findFirst().ifPresent(bodyData ->
                        extractDataFromEdsm(bodyData, location, sb)
                );
            } else { // no data available
                sb.append(" No data available for ").append(event.getBody()).append(".");
                sb.append(" Check gravity and temperature data before landing");
            }
        }

        playerSession.saveLocation(location);
        EventBusManager.publish(new SensorDataEvent(sb.toString(), "Warn user about planetary approach with these data. Temperature data is provided in K (Kelven) convert it to Celsius."));
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
        }
        sb.append(".");
        double surfaceTemperature = location.getSurfaceTemperature();
        location.setSurfaceTemperature(surfaceTemperature);
        sb.append(" Surface Temperature: ").append(surfaceTemperature).append(" K. ");
        if (location.isTidalLocked()) sb.append(" The planet is tidally locked. ");
    }


    private void extractDataFromEdsm(BodyData bodyData, LocationDto location, StringBuilder sb) {
        double gravity = formatDouble(bodyData.getGravity());
        location.setGravity(gravity);
        sb.append(" Surface Gravity: ").append(gravity).append("G. ");
        if (gravity > 1) {
            sb.append(" Gravity Warning!!! ");
        }
        Double surfaceTemperatureKelvin = bodyData.getSurfaceTemperature();
        location.setSurfaceTemperature(surfaceTemperatureKelvin);
        sb.append(" Surface Temperature: ").append(surfaceTemperatureKelvin).append(" K.");
        if (bodyData.getAtmosphereType() != null && !bodyData.getAtmosphereType().isEmpty()) {
            sb.append(" Atmosphere: ").append(bodyData.getAtmosphereType());
            sb.append(". ");
        }
        Map<String, Double> materials = bodyData.getMaterials();
        if (!materials.isEmpty()) {
            sb.append(" ").append(materials.size()).append(" materials detected. Details available on request. ");
            for (Map.Entry<String, Double> material : materials.entrySet()) {
                location.addMaterial(new MaterialDto(material.getKey(), material.getValue()));
            }
        }
        location.setPlanetData(bodyData);
    }
}
