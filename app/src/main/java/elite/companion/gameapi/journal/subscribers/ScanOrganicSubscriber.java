package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.ScanOrganicEvent;

public class ScanOrganicSubscriber {

    @Subscribe
    public void onScanOrganicEvent(ScanOrganicEvent event) {
        StringBuilder sb = new StringBuilder();
        String scanType = event.getScanType();
        if ("Sample".equals(scanType)) {
            sb.append("Organic sample detected: Genus:");
            sb.append(" ");
            sb.append(event.getGenusLocalised());
            sb.append(" Species:");
            sb.append(event.getVariantLocalised());
        } else if ("Analyse".equals(scanType)) {
            sb.append("Scans complete.");
        }

        EventBusManager.publish(new VoiceProcessEvent(sb.toString()));
    }
}
