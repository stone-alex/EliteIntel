package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class SAASignalsFoundSubscriber {

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.addSaaSignals(event.getSignals());
        playerSession.setCurrentLocation(currentLocation);

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

        if (signalsFound > 0) {
            int liveSignals = event.getGenuses() != null ? event.getGenuses().size() : 0;
            sb.append(" Signal(s) found: ");
            for (SAASignalsFoundEvent.Signal signal : signals) {
                sb.append(" Type: ").append(signal.getType()).append(". ");
                if ("Tritium".equals(signal.getType())) {
                    sb.append(" Carrier fuel source is detected. ");
                }
            }



            if (liveSignals > 0) {
                StellarObjectDto stellarObjectDto = playerSession.getStellarObjects().get(event.getBodyName());
                stellarObjectDto.setNumBioForms(liveSignals);
                stellarObjectDto.setGenus(event.getGenuses());
                playerSession.addStellarObject(stellarObjectDto);

                currentLocation.setGenus(event.getGenuses());
                playerSession.setCurrentLocation(currentLocation);

                sb.append(" Exobiology signal(s) found ").append(liveSignals).append(": ");
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    sb.append(" ");
                    sb.append(genus.getGenusLocalised());
                    sb.append(", ");
                }
            }
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        } else {
            EventBusManager.publish(new SensorDataEvent("No Signal(s) detected."));
        }
    }
}
