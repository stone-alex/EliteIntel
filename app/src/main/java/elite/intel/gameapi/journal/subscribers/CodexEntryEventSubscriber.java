package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.db.dao.CodexEntryDao;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.NavigationUtils;

import static elite.intel.util.StringUtls.capitalizeWords;

public class CodexEntryEventSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onCodexEntryEvent(CodexEntryEvent event) {

        final Status status = Status.getInstance();

        LocationDto currentLocation = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyID());
        playerSession.setCurrentLocationId(event.getBodyID(), event.getSystemAddress());
        StringBuilder sb = new StringBuilder();

        String firstWordOfEntryName = event.getNameLocalised().split(" ")[0];
        int bioSampleDistance = BioForms.getDistance(firstWordOfEntryName);
        BioForms.ProjectedPayment projectedPayment = BioForms.getAverageProjectedPayment(capitalizeWords(firstWordOfEntryName));
        String genus = bioSampleDistance > 0 ? capitalizeWords(firstWordOfEntryName) : null;

        boolean alreadyHaveThisEntry = codexEntryManager.checkIfExist(currentLocation.getStarName(), currentLocation.getBodyId(), event.getNameLocalised());


        if (!alreadyHaveThisEntry && event.isNewEntry()) {
            sb.append(" New Codex Entry: ");
        } else {
            sb.append(" Codex Entry: ");
        }
        sb.append(" Name: ");
        String[] split = event.getNameLocalised().split("-");
        sb.append(split[0]).append(", variant ").append(split[1]).append(", ");
        sb.append(" Category: ");
        sb.append(event.getSubCategoryLocalised()).append(". ");

        if (bioSampleDistance > 0 && !alreadyHaveThisEntry) {
            sb.append(" Minimum distance between samples for collection: ").append(bioSampleDistance).append(" meters. ");
        }


        if (!alreadyHaveThisEntry) {
            sb.append(", ");
            if (event.getVoucherAmount() > 0) {
                sb.append("Voucher Amount: ");
                sb.append(event.getVoucherAmount());
                sb.append(" credits");
            }
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
        } else {
            for (CodexEntryDao.CodexEntry entry : codexEntryManager.getForPlanet(currentLocation.getStarName(), currentLocation.getBodyId())) {
                boolean isNameMatched = entry.getEntryName().equals(event.getNameLocalised());
                double distanceFromPreviousSample = NavigationUtils.calculateSurfaceDistance(
                        status.getStatus().getLatitude(),
                        status.getStatus().getLongitude(),
                        entry.getLatitude(),
                        entry.getLongitude(),
                        status.getStatus().getPlanetRadius(),
                        0
                );
                if (genus != null && isNameMatched && distanceFromPreviousSample < bioSampleDistance) {
                    sb.append(" WARNING: Codex entry too proximate to previous sample of same genus- insufficient separation for sample diversity!");
                    break;
                }
            }
        }

        if(playerSession.isDiscoveryAnnouncementOn()) {
            String instructions = """
            Codex entry added. Provide essential summary.
            - Only facts, no speculation.
            - If there is a warning announce it, else do not mention that there are no warnings.
            - Do not append any extra data.
            - Be brief.
            - Spell out numerals.
            - Round rewards in credits to nearest million.
            """;
            EventBusManager.publish(new SensorDataEvent(sb.toString(), instructions));
        }
        if("$Codex_SubCategory_Organic_Structures;".equalsIgnoreCase(event.getSubCategory())) {
            codexEntryManager.save(event);
        }
        locationManager.save(currentLocation);
    }
}
