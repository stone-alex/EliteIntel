package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class FuelStateSubscriber {

    private boolean hasAnnounced = false;

    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        if(event.getFuel() == null) return;
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setStatus(event);
        double fuelMain = event.getFuel().getFuelMain();
        double fuelReservoir = event.getFuel().getFuelReservoir();

        if(fuelMain == 0) {
            //we are in SRV
            if(fuelReservoir <= 0.05 && !hasAnnounced){
                EventBusManager.publish(new VoiceProcessEvent("SRV Fuel Critical!"));
                hasAnnounced= true;
            }
        }
    }
}
