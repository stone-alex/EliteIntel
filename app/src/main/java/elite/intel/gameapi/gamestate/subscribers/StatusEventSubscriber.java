package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.session.PlayerSession;

public class StatusEventSubscriber {

    public static final int QUARTER_TANK_REMAINING = 25;
    public static final double BUGGY_STANDARD_FUEL_TANK = 0.50;

    @Subscribe
    public void onStatusChangedEvent(GameEvents.StatusEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        GameEvents.StatusEvent oldStatus = playerSession.getStatus();
        String legalStatusBeforeChange = oldStatus.getLegalState();

        if(oldStatus.getFuel() != null && playerSession.getShipLoadout() != null && playerSession.getShipLoadout().getFuelCapacity() !=null) {
            double fuelCapacityMain = playerSession.getShipLoadout().getFuelCapacity().getMain();
            if(fuelCapacityMain > 0) { // we are on the ship
                double fuelAmount = oldStatus.getFuel().getFuelMain();
                double remainingFuelInPercent = Math.round((fuelAmount / fuelCapacityMain * 100) * 100.0) / 100.0;
                if (remainingFuelInPercent != 0 && remainingFuelInPercent < QUARTER_TANK_REMAINING && event.getFuel().getFuelMain() > fuelAmount) {
                    EventBusManager.publish(new VoiceProcessEvent("Fuel warning: " + remainingFuelInPercent + "% remaining."));
                }
            } else { // we are in the buggy. use reservable instead
                double fuelAmount = oldStatus.getFuel().getFuelReservoir();
                double remainingFuelInPercent = Math.round((fuelAmount / BUGGY_STANDARD_FUEL_TANK * 100) * 100.0) / 100.0;
                if (remainingFuelInPercent != 0&& remainingFuelInPercent < QUARTER_TANK_REMAINING && event.getFuel().getFuelReservoir() > fuelAmount) {
                    EventBusManager.publish(new VoiceProcessEvent("Fuel warning: " + remainingFuelInPercent + "% remaining."));
                }

            }
        }

        if (legalStatusBeforeChange != null && !legalStatusBeforeChange.equalsIgnoreCase(event.getLegalState())) {
            EventBusManager.publish(new VoiceProcessEvent("Legal status changed to: " + event.getLegalState() + ". "));
        }

        playerSession.setStatus(event);

        if (event.getLatitude() > 0 && event.getLongitude() > 0 && event.getPlanetRadius() > 0) {
            EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius()));
        }
    }
}
