package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SupercruiseDestinationDropEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;

public class SuperCruiseDropSubscriber {

    @Subscribe
    public void onSuperCruiseDrop(SupercruiseDestinationDropEvent event) {

        if (event.getThreat() > 0) {
            EventBusManager.publish(new SensorDataEvent(" Dropped from supercruise. Threat level: " + event.getThreat() + ". ", "Notify user about supercruise exit and threat level"));
            return;
        }

        PlayerSession playerSession = PlayerSession.getInstance();
        String carrierName = playerSession.getCarrierData().getCarrierName();
        if (event.getType().toUpperCase().startsWith(carrierName.toUpperCase())) {
            EventBusManager.publish(new SensorDataEvent("Welcome Home to " + StringUtls.capitalizeWords(carrierName) + "! ", "Welcome user to home base"));
        }
    }
}
