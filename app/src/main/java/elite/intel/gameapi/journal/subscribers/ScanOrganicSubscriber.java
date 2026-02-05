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

    private static void announce(String sb) {
        if (PlayerSession.getInstance().isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb, """
                        Use ONLY facts from sensorData—no invention, external knowledge, or additions like planet/system/payment/bonus/vehicles/scan stages.
                        Rephrase to natural, immersive speech:
                            - Key elements ONLY: genus/species logged, distance/completion if stated.
                            - Use "we", "You" — NEVER "ship", "SRV", or "vehicle".
                            Examples of style (DO NOT copy—base on data):
                                - Data mentions genus logged → "<genus> sample logged."
                                - Data has distance → "First <genus> logged. Maintain 500 meters between colonies."
                                - Data signals complete → "<genus> scans complete."
                    
                            Output EXACTLY:
                                {"type": "chat", "response_text": "your natural rephrase", "action": "none", "params": {}, "expect_followup": false}
                    """));
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
        playerSession.setCurrentLocationId(event.getBody(), event.getSystemAddress());

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

        if (scan1.equals(scanType)) {
            sb.append(" Organic sample detected. Genus: ");
            sb.append(" ");
            sb.append("\"").append(genus).append("\"");
            sb.append(" Species:");
            sb.append(species);
            sb.append(" First sample out of three required. ");
            if (range != null) {
                sb.append(" Required Distance between samples: ");
                sb.append(range);
                sb.append(" meters. ");
            }

            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);
            bioSampleDto.setScanXof3(1);
            currentLocation.addBioScan(bioSampleDto);
            announce(sb.toString());

        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);
            currentLocation.addBioScan(bioSampleDto);
            bioSampleDto.setScanXof3(2);
            announce("Scan for genus \"" + genus + "\" logged. ");
        } else if (scan3.equalsIgnoreCase(scanType)) {
            sb = new StringBuilder();
            sb.append("Organic scans for genus: ");
            sb.append("\"").append(genus).append("\"");
            sb.append(" are complete. ");

            announce(sb.toString());
            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);

            bioSampleDto.setPayout(payment);
            bioSampleDto.setFistDiscoveryBonus(firstDiscoveryBonus);
            bioSampleDto.setScanXof3(3);
            bioSampleDto.setBioSampleCompleted(true);
            bioSampleDto.setOurDiscovery(currentLocation.isOurDiscovery());
            playerSession.addBioSample(bioSampleDto);
            currentLocation.deletePartialBioSamples();
            removeCodexEntry(event.getVariantLocalised(), event.getSystemAddress(), event.getBody());
            playerSession.clearGenusPaymentAnnounced();
        }

        locationManager.save(currentLocation);
    }

    private void removeCodexEntry(String variantLocalised, Long systemAddress, Long bodyId) {
        LocationDto address = locationManager.findBySystemAddress(systemAddress, bodyId);
        codexEntryManager.clearCompleted(address.getStarName(),address.getBodyId(),variantLocalised);
    }

    private BioSampleDto createBioSampleDto(String genus, String species, boolean isOurDiscovery) {

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
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
