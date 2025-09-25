package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.ScanOrganicEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.BioScanDistances;

import java.util.ArrayList;
import java.util.List;

public class ScanOrganicSubscriber {

    private final String scan1 = "Log";
    private final String scan2 = "Sample";
    private final String scan3 = "Analyse";
    PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onScanOrganicEvent(ScanOrganicEvent event) {
        StringBuilder sb = new StringBuilder();
        String scanType = event.getScanType();
        String genus = event.getGenusLocalised();
        String variant = event.getVariantLocalised();
        int planetNumber = event.getBody();
        long starSystemNumber = event.getSystemAddress();

        BioForms.BioDetails bioDetails = BioForms.getDetails(genus, variant);

        long valueInCredits = bioDetails == null ? 0 : bioDetails.creditValue();
        LocationDto currentLocation = playerSession.getCurrentLocation();

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
                sb.append("Approximate Vista Genomix payment: ");
                sb.append(valueInCredits);
                sb.append(" credits.");
            }

            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant, planetNumber, starSystemNumber, valueInCredits);
            bioSampleDto.setScanXof3("First of Three");
            currentLocation.clearBioSamples();
            currentLocation.addBioScan(bioSampleDto);
            EventBusManager.publish(new SensorDataEvent(sb.toString()));


        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant, planetNumber, starSystemNumber, valueInCredits);
            currentLocation.addBioScan(bioSampleDto);
            bioSampleDto.setScanXof3("Second of Three");
            EventBusManager.publish(new SensorDataEvent("Sample collected for: " + genus + "."));

        } else if (scan3.equalsIgnoreCase(scanType)) {
            EventBusManager.publish(new SensorDataEvent("Organic scans for" + genus + " are complete. "+(valueInCredits > 0 ? " approximate Vista Genomix payment: " + valueInCredits : "")));
            BioSampleDto bioSampleDto = createBioSampleDto(genus, variant, planetNumber, starSystemNumber, valueInCredits);
            bioSampleDto.setScanXof3("Three of Three");
            bioSampleDto.setBioSampleCompleted(true);
            playerSession.addBioSample(bioSampleDto);
            playerSession.saveCurrentLocation(currentLocation);
            removeCodexEntry(event.getVariantLocalised(), playerSession);
            currentLocation.clearBioSamples();
        }

    }

    private void removeCodexEntry(String variantLocalised, PlayerSession playerSession) {
        List<CodexEntryEvent> codexEntries = playerSession.getCodexEntries();
        if(codexEntries == null || codexEntries.isEmpty()) return;
        List<CodexEntryEvent> adjusted = new ArrayList<>();
        for(CodexEntryEvent entry : codexEntries){
            if(!entry.getNameLocalised().equalsIgnoreCase(variantLocalised)){
                adjusted.add(entry);
            }
        }
        playerSession.setCodexEntries(adjusted);
    }

    private BioSampleDto createBioSampleDto(String genus, String variant, int planetNumber, long starSystemNumber, long valueInCredits) {
        BioSampleDto bioSampleDto = new BioSampleDto();
        bioSampleDto.setScanLatitude(playerSession.getStatus().getLatitude());
        bioSampleDto.setScanLongitude(playerSession.getStatus().getLongitude());
        bioSampleDto.setGenus(genus);
        bioSampleDto.setSpecies(variant);
        bioSampleDto.setPlanetNumber(planetNumber);
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
