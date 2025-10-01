package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.capitalizeWords;

public class CodexEntryEventSubscriber {

    @Subscribe
    public void onCodexEntryEvent(CodexEntryEvent event) {
        StringBuilder sb = new StringBuilder();

        String firstWordOfEntryName = event.getNameLocalised().split(" ")[0];
        long projectedPayment = BioForms.getAverageProjectedPayment(capitalizeWords(firstWordOfEntryName));
        if(event.isNewEntry()) {
            sb.append("New Codex Entry: ");
        } else {
            sb.append("Codex Entry: ");
        }
        sb.append("Category: ");
        sb.append(event.getCategoryLocalised());
        sb.append(", ");
        sb.append("Name: ");
        sb.append(event.getNameLocalised());
        sb.append(", ");
        sb.append("Voucher Amount: ");
        sb.append(event.getVoucherAmount());
        sb.append(" credits.");
        if(projectedPayment > 0) sb.append(" Projected Vista Genomics Payment: ").append(projectedPayment).append(" credits. For a complete set of three samples");
        EventBusManager.publish(new SensorDataEvent(sb.toString()));

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.addCodexEntry(event);
        playerSession.saveCurrentLocation(currentLocation);
    }
}
