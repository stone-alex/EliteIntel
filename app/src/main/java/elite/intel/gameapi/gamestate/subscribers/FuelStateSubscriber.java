package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class FuelStateSubscriber {

    public static final int QUARTER_TANK_REMAINING = 25;
    private boolean hasAnnounced = false;

    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        if (event.getFuel() == null) return;
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        GameEvents.StatusEvent oldStatus = status.getStatus();
        status.setStatus(event);
        double fuelMain = event.getFuel().getFuelMain();
        double fuelReservoir = event.getFuel().getFuelReservoir();


        if (status.isInSrv()) {
            //TODO Need a way to know we are in SRV
            if (fuelReservoir <= 0.06 && !hasAnnounced) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("SRV Fuel Critical!"));
                hasAnnounced = true;
            } else {
                hasAnnounced = false;
            }
        } else if (status.isInMainShip()) {
            //We are on the ship.
            if (!hasAnnounced && oldStatus != null && oldStatus.getFuel() != null && playerSession.getShipLoadout() != null && playerSession.getShipLoadout().getFuelCapacity() != null) {
                double fuelCapacityMain = playerSession.getShipLoadout().getFuelCapacity().getMain();
                double fuelAmount = oldStatus.getFuel().getFuelMain();
                double remainingFuelInPercent = Math.round((fuelAmount / fuelCapacityMain * 100) * 100.0) / 100.0;
                if (remainingFuelInPercent != 0 && remainingFuelInPercent < QUARTER_TANK_REMAINING && event.getFuel().getFuelMain() > fuelAmount) {
                    EventBusManager.publish(new MissionCriticalAnnouncementEvent("Fuel warning: " + remainingFuelInPercent + "% remaining."));
                    hasAnnounced = true;
                } else {
                    hasAnnounced = false;
                }
            }
        }
    }
}
