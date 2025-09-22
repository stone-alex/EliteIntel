package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ScanOrganicEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.BioScanDistances;

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

            BioSampleDto bioSampleDto = new BioSampleDto();
            bioSampleDto.setScanLatitude(playerSession.getStatus().getLatitude());
            bioSampleDto.setScanLongitude(playerSession.getStatus().getLongitude());
            bioSampleDto.setGenus(genus);
            bioSampleDto.setDistanceToNextSample(BioScanDistances.GENUS_TO_CCR.get(genus));
            playerSession.addBioSample(bioSampleDto);
            EventBusManager.publish(new SensorDataEvent(sb.toString()));

        } else if (scan2.equalsIgnoreCase(scanType)) {
            BioSampleDto bioSampleDto = new BioSampleDto();
            bioSampleDto.setScanLatitude(playerSession.getStatus().getLatitude());
            bioSampleDto.setScanLongitude(playerSession.getStatus().getLongitude());
            bioSampleDto.setGenus(genus);
            bioSampleDto.setDistanceToNextSample(BioScanDistances.GENUS_TO_CCR.get(genus));
            playerSession.addBioSample(bioSampleDto);
            EventBusManager.publish(new SensorDataEvent("Sample collected for: " + genus + "."));

        } else if (scan3.equalsIgnoreCase(scanType)) {
            EventBusManager.publish(new SensorDataEvent("Organic scans for" + genus + " are complete."));
            BioSampleDto bioSampleDto = new BioSampleDto();
            bioSampleDto.setGenus(genus);
            bioSampleDto.setScanLatitude(playerSession.getStatus().getLatitude());
            bioSampleDto.setScanLongitude(playerSession.getStatus().getLongitude());
            bioSampleDto.setBioSampleCompleted(true);
            LocationDto currentLocation = playerSession.getCurrentLocation();
            currentLocation.addBioScan(bioSampleDto);
            playerSession.setCurrentLocation(currentLocation);
            playerSession.clearBioSamples();
        }

    }
}
