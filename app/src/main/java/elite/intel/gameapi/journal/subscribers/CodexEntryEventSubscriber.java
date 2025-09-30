package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class CodexEntryEventSubscriber {

    @Subscribe
    public void onCodexEntryEvent(CodexEntryEvent event) {
        if(event.isNewEntry()) {
            StringBuilder sb = new StringBuilder();
            sb.append("New Codex Entry: ");
            sb.append("Category: ");
            sb.append(event.getCategoryLocalised());
            sb.append(", ");
            sb.append("Name: ");
            sb.append(event.getNameLocalised());
            sb.append(", ");
            sb.append("Voucher Amount: ");
            sb.append(event.getVoucherAmount());
            sb.append(" credits.");
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.addCodexEntry(event);
        playerSession.saveCurrentLocation(currentLocation);
    }
}
