package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.ClipboardUtils;
import elite.intel.util.FleetCarrierRouteCalculator;

import java.util.Objects;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        double[] starPos = event.getStarPos();
        playerSession.setLastKnownCarrierLocation(starSystem);


        FleetCarrierRouteManager fleetCarrierRouteManager = FleetCarrierRouteManager.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        CarrierJump currentLocationLeg = fleetCarrierRouteManager.findByPrimaryStar(event.getStarSystem());
        boolean currentLegIsNotPresent = currentLocationLeg == null;
        boolean routePlotted = fleetCarrierRouteManager.getFleetCarrierRoute().size() > 0;

        if (currentLegIsNotPresent && routePlotted){
            String systemName = fleetCarrierRouteManager
                    .getFleetCarrierRoute()
                    .get(fleetCarrierRouteManager.getFleetCarrierRoute().size() - 1)
                    .getSystemName();
            ClipboardUtils.setClipboardText(systemName);
            FleetCarrierRouteCalculator.calculate();
        }

        int fuelUsed = currentLocationLeg.getFuelUsed();
        carrierData.setFuelLevel(carrierData.getFuelLevel() - fuelUsed);
        playerSession.setCarrierData(carrierData);

        fleetCarrierRouteManager.removeLeg(event.getStarSystem());

        playerSession.setCarrierDepartureTime(null);

        Status status = Status.getInstance();
        String stationName = playerSession.getCurrentLocation().getStationName();
        CarrierDataDto carrierInfo = playerSession.getCarrierData();

        if (status.isDocked()) {
            playerSession.saveLocation(toLocationDto(event));
        }

        if (carrierData != null && starPos[0] > 0) {
            carrierData.setX(starPos[0]);
            carrierData.setY(starPos[1]);
            carrierData.setZ(starPos[2]);
            carrierData.setStarName(starSystem);
            playerSession.setCarrierData(carrierData);
        }

        EventBusManager.publish(new SensorDataEvent("Carrier Location: " + event.getStarSystem()));
    }

    private LocationDto toLocationDto(CarrierJumpEvent event) {
        LocationManager locationData = LocationManager.getInstance();
        LocationDto location = locationData.getLocation(event.getStarSystem(), event.getBodyId());
        return fillInWhatWeCan(event, Objects.requireNonNullElseGet(location, () -> new LocationDto(event.getBodyId(), event.getStarSystem())));
    }

    private LocationDto fillInWhatWeCan(CarrierJumpEvent event, LocationDto location) {
        location.setAllegiance(event.getSystemAllegiance());
        location.setX(event.getStarPos()[0]);
        location.setY(event.getStarPos()[1]);
        location.setZ(event.getStarPos()[2]);
        location.setStationGovernment(event.getSystemGovernmentLocalised());
        location.setPlanetName(event.getBody());
        location.setAllegiance(event.getSystemAllegiance());
        location.setControllingPower(event.getControllingPower());
        //...
        return location;
    }
}