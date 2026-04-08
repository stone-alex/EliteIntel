package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.DeferredNotificationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpRequestEvent;
import elite.intel.session.PlayerSession;

import java.time.Duration;
import java.time.Instant;

@SuppressWarnings("unused")
public class CarrierJumpRequestSubscriber {

    @Subscribe
    public void onCarrierJumpRequestEvent(CarrierJumpRequestEvent event) {
        Thread.ofVirtual().start(() -> {
            String destinationStellarBody = event.getBody();
            String rawDepartureTime = event.getDepartureTime();

            Instant departureInstant = Instant.parse(rawDepartureTime);
            long totalMinutes = Duration.between(Instant.now(), departureInstant).toMinutes();
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;

            String timeUntil;
            if (hours > 0 && minutes > 0) {
                timeUntil = hours + (hours == 1 ? " hour" : " hours") + " and " + minutes + (minutes == 1 ? " minute" : " minutes");
            } else if (hours > 0) {
                timeUntil = hours + (hours == 1 ? " hour" : " hours");
            } else {
                timeUntil = minutes + (minutes == 1 ? " minute" : " minutes");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Carrier scheduled to depart");
            if (destinationStellarBody != null && !destinationStellarBody.isBlank()) {
                sb.append(" to ").append(destinationStellarBody);
            }
            sb.append(" in ").append(timeUntil).append(".");

            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.setCarrierDepartureTime(rawDepartureTime);

            long millis = Instant.parse(event.getDepartureTime()).toEpochMilli() - (1000 * 60 * 3);
            DeferredNotificationManager.getInstance().scheduleNotification("Carrier is departing in three minutes.", millis);
            EventBusManager.publish(new SensorDataEvent(sb.toString(), "Notify User"));
        });
    }
}
