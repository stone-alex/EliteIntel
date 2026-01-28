package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.DeferredNotificationManager;
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
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.ClipboardUtils;
import elite.intel.util.FleetCarrierRouteCalculator;

import java.util.Objects;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {
    private static final Long FOUR_MINUTES = (long) (1000 * 60 * 4);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        double[] starPos = event.getStarPos();
        playerSession.setLastKnownCarrierLocation(starSystem);

        if (starPos.length == 3 && starPos[0] == 0.0 && starPos[1] == 0.0 && starPos[2] == 0) {
            EventBusManager.publish(new AppLogEvent("WARNING: Carrier Jump complete, but star position is reported 0.0.0"));
        }


        FleetCarrierRouteManager fleetCarrierRouteManager = FleetCarrierRouteManager.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        CarrierJump currentLocationLeg = fleetCarrierRouteManager.findByPrimaryStar(event.getStarSystem());
        boolean currentLegIsNotPresent = currentLocationLeg == null;
        boolean routePlotted = !fleetCarrierRouteManager.getFleetCarrierRoute().isEmpty();

        if (currentLegIsNotPresent && routePlotted) {
            String systemName = fleetCarrierRouteManager
                    .getFleetCarrierRoute()
                    .get(fleetCarrierRouteManager.getFleetCarrierRoute().size() - 1)
                    .getSystemName();
            ClipboardUtils.setClipboardText(systemName);
            FleetCarrierRouteCalculator.calculate();
        }

        int fuelUsed = currentLocationLeg == null ? 0 : currentLocationLeg.getFuelUsed();
        carrierData.setFuelLevel(carrierData.getFuelLevel() - fuelUsed);
        playerSession.setCarrierData(carrierData);

        fleetCarrierRouteManager.removeLeg(event.getStarSystem());

        playerSession.setCarrierDepartureTime(null);

        Status status = Status.getInstance();
        String stationName = playerSession.getCurrentLocation().getStationName();
        CarrierDataDto carrierInfo = playerSession.getCarrierData();

        LocationDto location = toLocationDto(event);
        if (status.isDocked()) {
            // NOTE: Assumption: we are docked at the carrier on arrival, not at some other station.
            // NOTE: This will cause problems if we jump carrier while sitting on another station, until player jumps to another system

            location.setStationName(stationName);
            location.setX(starPos[0]);
            location.setY(starPos[1]);
            location.setZ(starPos[2]);
            playerSession.setCurrentLocationId(location.getBodyId());
            playerSession.setCurrentPrimaryStarName(starSystem);
            playerSession.saveLocation(location);
        }

        if (starPos[0] > 0) {
            carrierData.setX(starPos[0]);
            carrierData.setY(starPos[1]);
            carrierData.setZ(starPos[2]);
            carrierData.setStarName(starSystem);
            playerSession.setCarrierData(carrierData);
        }

        CarrierDataDto postJumpCarrierData = playerSession.getCarrierData();
        int numJumpsRemaining = fleetCarrierRouteManager.getFleetCarrierRoute().size();
        int estimatedTimeToFinal = numJumpsRemaining * 20;
        String remainingRoute = numJumpsRemaining == 0 ? ". Final destination reached!" : ". Remaining " + numJumpsRemaining + " jumps. Estimated time to final " + estimatedTimeToFinal;

        String instructions = """
                    Notify user about new carrier location.
                    Example: Carrier jump complete!. New location <starSystem>, remaining fuel supply <fuelSupply> tons. Fuel in reserve <fuelReserve> tons.
                """;
        EventBusManager.publish(
                new SensorDataEvent(
                        "Carrier Location: " + event.getStarSystem() + " fuelSupply " + postJumpCarrierData.getFuelLevel() + " fuelReserve:" + postJumpCarrierData.getFuelReserve() + remainingRoute,
                        instructions
                )
        );
        DeferredNotificationManager.getInstance().scheduleNotification("Carrier jump cooldown is complete", FOUR_MINUTES);
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