package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ScanOrganicEvent;

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
