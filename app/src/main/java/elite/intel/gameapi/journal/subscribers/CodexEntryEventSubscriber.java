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
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getLocation(event.getBodyID());
        playerSession.setCurrentLocationId(event.getBodyID());
        StringBuilder sb = new StringBuilder();

        String firstWordOfEntryName = event.getNameLocalised().split(" ")[0];
        int bioSampleDistance = BioForms.getDistance(firstWordOfEntryName);
        BioForms.ProjectedPayment projectedPayment = BioForms.getAverageProjectedPayment(capitalizeWords(firstWordOfEntryName));
        String genus = bioSampleDistance > 0 ? capitalizeWords(firstWordOfEntryName) : null;

        boolean alreadyHaveThisEntry = currentLocation.getCodexEntries().stream().anyMatch(entry -> entry.getNameLocalised().equals(event.getNameLocalised()));

        if(!alreadyHaveThisEntry && event.isNewEntry()) {
            sb.append("New Codex Entry: ");
        } else {
            sb.append("Codex Entry: ");
        }
        sb.append("Category: ");
        sb.append(event.getCategoryLocalised());
        sb.append(", ");
        sb.append("Name: ");
        sb.append(event.getNameLocalised());

        if(bioSampleDistance > 0 && !alreadyHaveThisEntry) {
            sb.append(" Distance between samples: ").append(bioSampleDistance).append(" meters. ");
        }



        if (!alreadyHaveThisEntry) {
            sb.append(", ");
            sb.append("Voucher Amount: ");
            sb.append(event.getVoucherAmount());
            sb.append(" credits (one per planet only).");
            Boolean isAnnounced = playerSession.paymentHasBeenAnnounced(genus);

            if (projectedPayment != null && projectedPayment.payment() != null && !isAnnounced) {
                sb.append("Vista Genomics Payment: ").append(projectedPayment).append(" credits. For a complete set of three samples");
                if (projectedPayment.firstDiscoveryBonus() != null && currentLocation.isOurDiscovery()) {
                    sb.append(", plus");
                    sb.append(projectedPayment.firstDiscoveryBonus());
                    sb.append(" bonus for first discovery.");
                }
                sb.append(".");
                playerSession.setGenusPaymentAnnounced(genus);
            }
        }

        EventBusManager.publish(new SensorDataEvent(sb.toString()));

        currentLocation.addCodexEntry(event);
        playerSession.saveLocation(currentLocation);
    }
}
