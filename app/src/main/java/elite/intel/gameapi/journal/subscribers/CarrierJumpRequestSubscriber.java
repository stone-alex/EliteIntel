package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.DeferredNotificationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpRequestEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimestampFormatter;

import java.time.Instant;

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

        long millis = Instant.parse(event.getDepartureTime()).toEpochMilli();
        DeferredNotificationManager.getInstance().scheduleNotification("Carrier is departing.", millis);
        EventBusManager.publish(new SensorDataEvent(sb.toString(), "Notify User"));
    }
}
