package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.SAASignalsFoundEvent;
import elite.companion.util.EventBusManager;

import java.util.List;

public class SAASignalsFoundSubscriber {

    @Subscribe
    public void onSAASignalsFound(SAASignalsFoundEvent event) {
        StringBuilder sb = new StringBuilder();

        List<SAASignalsFoundEvent.Signal> signals = event.getSignals();
        int signalsFound = signals != null ? signals.size() : 0;

        if (signalsFound > 0) {
            for (SAASignalsFoundEvent.Signal signal : signals) {
                sb.append(" Type: ").append(signal.getTypeLocalised()).append(". ");
            }

            int liveSignals = event.getGenuses() != null ? event.getGenuses().size() : 0;
            sb.append(" Exobiology signal(s) found: ").append(liveSignals).append(".");

            if (liveSignals > 0) {
                for (SAASignalsFoundEvent.Genus genus : event.getGenuses()) {
                    sb.append(" ");
                    sb.append(genus.getGenusLocalised());
                    sb.append(", ");
                }
            }

            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }
}
