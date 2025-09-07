package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.SAASignalsFoundEvent;

import java.util.List;

public class SAASignalsFoundSubscriber {

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

        if (signalsFound > 0) {
            sb.append(" Signal(s) found: ").append(signalsFound).append(".");
            for (SAASignalsFoundEvent.Signal signal : signals) {
                sb.append(" Type: ").append(signal.getType()).append(". ");
                if ("Tritium".equals(signal.getType())) {
                    sb.append("Carrier fuel source is detected. ");
                }
            }

            int liveSignals = event.getGenuses() != null ? event.getGenuses().size() : 0;


            if (liveSignals > 0) {
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    sb.append(" ");
                    sb.append(genus.getGenusLocalised());
                    sb.append(", ");
                }
                sb.append(" Exobiology signal(s) found: ").append(liveSignals).append(".");
            }
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        } else {
            EventBusManager.publish(new SensorDataEvent("No Signal(s) detected."));
        }
    }
}
