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

public class ScanOrganicSubscriber {

    private final String scan1 = "Log";
    private final String scan2 = "Sample";
    private final String scan3 = "Analyse";
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final Status status = Status.getInstance();

    @Subscribe
    public void onScanOrganicEvent(ScanOrganicEvent event) {
        playerSession.setTracking(new TargetLocation()); // turn off tracking
        StringBuilder sb = new StringBuilder();
        String scanType = event.getScanType();
        String genus = event.getGenusLocalised();
        String variant = event.getVariantLocalised();
        long starSystemNumber = event.getSystemAddress();

        long valueInCredits = BioForms.getAverageProjectedPayment(genus);
        BioForms.BioDetails bioDetails = BioForms.getDetails(genus, variant);
        int range = bioDetails == null ? BioScanDistances.GENUS_TO_CCR.get(genus) : bioDetails.colonyRange();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        boolean isOurDiscovery = currentLocation.isOurDiscovery();
        removeCodexEntryIfMatches(event.getVariantLocalised(), range, true);

        if (scan1.equals(scanType)) {
            sb.append(" Organic sample detected: Genus: ");
            sb.append(" ");
            sb.append(genus);
            sb.append(" Species:");
            sb.append(variant);
            sb.append(" First sample out of three required. ");
            sb.append(" Required Distance between samples: ");
            sb.append(BioScanDistances.GENUS_TO_CCR.get(genus));
            sb.append(" meters. ");

            if(valueInCredits > 0) {
                sb.append("Approximate Vista Genomics payment: ");
                sb.append(valueInCredits);
                if (isOurDiscovery) {
                    sb.append(" credits,");
                    sb.append(" Plus bonus for first discovery.");
                }
                sb.append(" credits.");
            }

            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant, starSystemNumber, valueInCredits);
            bioSampleDto.setScanXof3("First of Three");
            currentLocation.addBioScan(bioSampleDto);
            announce(sb.toString());


        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant,  starSystemNumber, valueInCredits);
            currentLocation.addBioScan(bioSampleDto);
            bioSampleDto.setScanXof3("Second of Three");
            announce("Sample collected for: " + genus + ".");

        } else if (scan3.equalsIgnoreCase(scanType)) {
            sb = new StringBuilder();
            sb.append("Organic scans for: ");
            sb.append(genus);
            sb.append(" are complete. ");
            if (valueInCredits > 0) {
                sb.append(" approximate Vista Genomics payment: ");
                sb.append(valueInCredits);
                if (isOurDiscovery) sb.append("credits, plus bonus for first discovery.");
            } else {
                sb.append(" credits.");
            }


            announce(sb.toString());
            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant,  starSystemNumber, valueInCredits);
            bioSampleDto.setScanXof3("Three of Three");
            bioSampleDto.setBioSampleCompleted(true);
            playerSession.addBioSample(bioSampleDto);
            playerSession.saveCurrentLocation(currentLocation);
            playerSession.getCurrentLocation().deletePartialBioSamples();
            playerSession.save();
            removeCodexEntryIfMatches(event.getVariantLocalised(), -1, false);
        }
    }

    private static void announce(String sb) {
        if(PlayerSession.getInstance().isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new SensorDataEvent(sb));
        }
    }


    private void removeCodexEntryIfMatches(String variantLocalised, int range, boolean useDistance) {

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
        playerSession.saveCurrentLocation(currentLocation);
    }


    private BioSampleDto createBioSampleDto(String genus, String variant, long starSystemNumber, long valueInCredits) {

        BioSampleDto bioSampleDto = new BioSampleDto();
        bioSampleDto.setPlanetName(playerSession.getCurrentLocation().getPlanetName());
        bioSampleDto.setScanLatitude(status.getStatus().getLatitude());
        bioSampleDto.setScanLongitude(status.getStatus().getLongitude());
        bioSampleDto.setGenus(genus);
        bioSampleDto.setSpecies(variant);
        bioSampleDto.setBodyId(playerSession.getCurrentLocation().getBodyId());
        bioSampleDto.setStarSystemNumber(starSystemNumber);
        bioSampleDto.setDistanceToNextSample(distanceToNextSample(genus, variant));
        bioSampleDto.setPayout(valueInCredits);
        return bioSampleDto;
    }

    private double distanceToNextSample(String genus, String species){
        BioForms.BioDetails details = BioForms.getDetails(genus, species);
        return details == null? BioScanDistances.GENUS_TO_CCR.get(genus): details.colonyRange();
    }
}
