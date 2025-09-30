package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpRequestEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimestampFormatter;

import static elite.intel.session.PlayerSession.CARRIER_DEPARTURE_TIME;

@SuppressWarnings("unused")
public class CarrierJumpRequestSubscriber {

    @Subscribe
    public void onCarrierJumpRequestEvent(CarrierJumpRequestEvent event) {

        String fleetCarrierType = event.getCarrierType(); //nulls
        String destinationSystemName = event.getSystemName(); //nulls
        String destinationStellarBody = event.getBody();
        String departureTime = TimestampFormatter.formatTimestamp(event.getDepartureTime(), true);

        StringBuilder sb = new StringBuilder();
        sb.append("Carrier Jump Scheduled: ");
        sb.append(" to ");
        sb.append(destinationStellarBody);
        sb.append(" departure time: ");
        sb.append(departureTime);
        sb.append(".");

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setCarrierDepartureTime(departureTime);

        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}
