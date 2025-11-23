package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.ScanOrganicEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.BioScanDistances;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;
import static elite.intel.util.StringUtls.subtractString;

public class ScanOrganicSubscriber {

    private final String scan1 = "Log";
    private final String scan2 = "Sample";
    private final String scan3 = "Analyse";
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final Status status = Status.getInstance();
    private int scanCount = 0;

    @Subscribe
    public void onScanOrganicEvent(ScanOrganicEvent event) {
        playerSession.setTracking(new TargetLocation()); // turn off tracking
        StringBuilder sb = new StringBuilder();
        String scanType = event.getScanType();
        String genus = event.getGenusLocalised();
        String species = subtractString(event.getSpeciesLocalised(), genus);
        LocationDto currentLocation = playerSession.getLocation(event.getBody(), playerSession.getPrimaryStarName());
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

        removeCodexEntryIfMatches(event.getVariantLocalised(), range, true);
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

            if (payment > 0 && !isAnnounced) {
                sb.append("Vista Genomics payment: ");
                sb.append(payment);
                sb.append(" credits,");
                if (isOurDiscovery && firstDiscoveryBonus > 0) {
                    sb.append(" Plus bonus ");
                    sb.append(firstDiscoveryBonus);
                    sb.append(" credits for first discovery.");
                }
            }

            BioSampleDto bioSampleDto = createBioSampleDto(genus, species, isOurDiscovery);
            bioSampleDto.setScanXof3("First of Three");
            currentLocation.addBioScan(bioSampleDto);
            announce(sb.toString());
            scanCount = 1;

        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = createBioSampleDto(genus, species,  isOurDiscovery);
            currentLocation.addBioScan(bioSampleDto);
            bioSampleDto.setScanXof3("Second of Three");
            if(scanCount == 1) {
                announce("Sample collected for: " + genus + ".");
            }
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
            removeCodexEntryIfMatches(event.getVariantLocalised(), -1, false);
            scanCount = 0;
            playerSession.clearGenusPaymentAnnounced();
        }

        playerSession.saveLocation(currentLocation);
    }

    private static void announce(String sb) {
        if(PlayerSession.getInstance().isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb));
        }
    }


    private void removeCodexEntryIfMatches(String variantLocalised, Integer range, boolean useDistance) {
        if (range == null) return;

        double latitude = status.getStatus().getLatitude();
        double longitude = status.getStatus().getLongitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<CodexEntryEvent> codexEntries = currentLocation.getCodexEntries();
        if(codexEntries == null || codexEntries.isEmpty()) return;

        List<CodexEntryEvent> adjusted = new ArrayList<>();
        for(CodexEntryEvent entry : codexEntries){
            boolean notMatchingBioForm = entry.getNameLocalised() == null || !entry.getNameLocalised().equalsIgnoreCase(variantLocalised);
            if (useDistance) {
                double codexLatitude = entry.getLatitude();
                double codexLongitude = entry.getLongitude();
                double distanceFromSample = calculateSurfaceDistance(latitude, longitude, codexLatitude, codexLongitude, planetRadius, 0);
                boolean farFromMe = distanceFromSample > (range == 0 ? 200 : range);
                if (notMatchingBioForm || farFromMe) {
                    adjusted.add(entry); //KEEP!
                }
            } else {
                if (notMatchingBioForm) {
                    adjusted.add(entry); //KEEP!
                }
            }
        }
        currentLocation.setCodexEntries(adjusted);
        playerSession.saveLocation(currentLocation);
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

    private double distanceToNextSample(String genus, String species){
        BioForms.BioDetails details = BioForms.getDetails(genus, species);
        return details == null ? BioScanDistances.GENUS_TO_CCR.get(genus): details.colonyRange();
    }
}
