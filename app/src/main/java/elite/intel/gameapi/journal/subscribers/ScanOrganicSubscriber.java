package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.ScanOrganicEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.BioScanDistances;

import static elite.intel.util.StringUtls.subtractString;

public class ScanOrganicSubscriber {

    private final String scan1 = "Log";
    private final String scan2 = "Sample";
    private final String scan3 = "Analyse";
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
    private final Status status = Status.getInstance();
    private int scanCount = 0;

    private static void announce(String sb) {
        if (PlayerSession.getInstance().isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb));
        }
    }

    @Subscribe
    public void onScanOrganicEvent(ScanOrganicEvent event) {
        playerSession.setTracking(new TargetLocation()); // turn off tracking
        StringBuilder sb = new StringBuilder();
        String scanType = event.getScanType();
        String genus = event.getGenusLocalised();
        String species = subtractString(event.getSpeciesLocalised(), genus);
        LocationDto currentLocation = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBody());
        playerSession.setCurrentLocationId(event.getBody());

        boolean isOurDiscovery = currentLocation.isOurDiscovery();
        BioForms.ProjectedPayment paymentData = BioForms.getProjectedPayment(genus, species);

        long payment = paymentData == null ? 0 : paymentData.payment();
        long firstDiscoveryBonus = paymentData == null || !isOurDiscovery ? 0 : paymentData.firstDiscoveryBonus();

        BioForms.BioDetails bioDetails = BioForms.getDetails(genus, species);
        Integer distance = BioScanDistances.GENUS_TO_CCR.get(genus);

        Integer range = null;
        if (bioDetails == null) {
            range = distance;
        }
        if (distance != null) {
            range = distance;
        }

        Boolean isAnnounced = playerSession.paymentHasBeenAnnounced(genus);

        if (scan1.equals(scanType)) {
            sb.append(" Organic sample detected: Genus: ");
            sb.append(" ");
            sb.append(genus);
            sb.append(" Species:");
            sb.append(species);
            sb.append(" First sample out of three required. ");
            if (range != null) {
                sb.append(" Required Distance between samples: ");
                sb.append(range);
                sb.append(" meters. ");
            }

            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);
            bioSampleDto.setScanXof3("First of Three");
            currentLocation.addBioScan(bioSampleDto);
            announce(sb.toString());
            scanCount = 1;

        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);
            currentLocation.addBioScan(bioSampleDto);
            bioSampleDto.setScanXof3("Second of Three");
            scanCount = 2;

        } else if (scan3.equalsIgnoreCase(scanType)) {
            sb = new StringBuilder();
            sb.append("Organic scans for: ");
            sb.append(genus);
            sb.append(" are complete. ");

            announce(sb.toString());
            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);

            bioSampleDto.setPayout(payment);
            bioSampleDto.setFistDiscoveryBonus(firstDiscoveryBonus);
            bioSampleDto.setScanXof3("Three of Three");
            bioSampleDto.setBioSampleCompleted(true);
            bioSampleDto.setOurDiscovery(currentLocation.isOurDiscovery());
            playerSession.addBioSample(bioSampleDto);
            currentLocation.deletePartialBioSamples();
            removeCodexEntry(event.getVariantLocalised());
            scanCount = 0;
            playerSession.clearGenusPaymentAnnounced();
        }

        playerSession.saveLocation(currentLocation);
    }

    private void removeCodexEntry(String variantLocalised) {
        codexEntryManager.clearCompleted(
                playerSession.getCurrentLocation().getStarName(),
                playerSession.getCurrentLocation().getBodyId(),
                variantLocalised
        );
    }


    private BioSampleDto createBioSampleDto(String genus, String species, boolean isOurDiscovery) {

        LocationDto currentLocation = playerSession.getCurrentLocation();
        BioSampleDto bioSampleDto = new BioSampleDto();
        bioSampleDto.setPrimaryStar(playerSession.getPrimaryStarName());
        bioSampleDto.setPlanetName(currentLocation.getPlanetName());
        bioSampleDto.setPlanetShortName(currentLocation.getPlanetShortName());
        bioSampleDto.setScanLatitude(status.getStatus().getLatitude());
        bioSampleDto.setScanLongitude(status.getStatus().getLongitude());
        bioSampleDto.setGenus(genus);
        bioSampleDto.setSpecies(species);
        bioSampleDto.setOurDiscovery(isOurDiscovery);
        bioSampleDto.setBodyId(currentLocation.getBodyId());
        bioSampleDto.setDistanceToNextSample(distanceToNextSample(genus, species));
        return bioSampleDto;
    }

    private double distanceToNextSample(String genus, String species) {
        BioForms.BioDetails details = BioForms.getDetails(genus, species);
        return details == null ? BioScanDistances.GENUS_TO_CCR.get(genus) : details.colonyRange();
    }
}
