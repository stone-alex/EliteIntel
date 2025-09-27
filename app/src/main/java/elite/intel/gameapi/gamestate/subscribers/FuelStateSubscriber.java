package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class FuelStateSubscriber {

    public static final int QUARTER_TANK_REMAINING = 25;
    private boolean hasAnnounced = false;

    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        if (event.getFuel() == null) return;
        PlayerSession playerSession = PlayerSession.getInstance();
        GameEvents.StatusEvent oldStatus = playerSession.getStatus();
        playerSession.setStatus(event);
        double fuelMain = event.getFuel().getFuelMain();
        double fuelReservoir = event.getFuel().getFuelReservoir();


        if (event.getAltitude() == 0 && event.getLatitude() > 0) {
            //We are not flying
            if (fuelReservoir <= 0.06 && !hasAnnounced) {
                EventBusManager.publish(new VoiceProcessEvent("SRV Fuel Critical!"));
                hasAnnounced = true;
            } else {
                hasAnnounced = false;
            }
        } else {
            //We are on the ship.
            if (!hasAnnounced && oldStatus.getFuel() != null && playerSession.getShipLoadout() != null && playerSession.getShipLoadout().getFuelCapacity() != null) {
                double fuelCapacityMain = playerSession.getShipLoadout().getFuelCapacity().getMain();
                double fuelAmount = oldStatus.getFuel().getFuelMain();
                double remainingFuelInPercent = Math.round((fuelAmount / fuelCapacityMain * 100) * 100.0) / 100.0;
                if (remainingFuelInPercent != 0 && remainingFuelInPercent < QUARTER_TANK_REMAINING && event.getFuel().getFuelMain() > fuelAmount) {
                    EventBusManager.publish(new VoiceProcessEvent("Fuel warning: " + remainingFuelInPercent + "% remaining."));
                    hasAnnounced = true;
                } else {
                    hasAnnounced = false;
                }
            }
        }
    }
}
