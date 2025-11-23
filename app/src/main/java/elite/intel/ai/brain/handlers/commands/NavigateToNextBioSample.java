package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;

public class NavigateToNextBioSample implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(NavigateToNextBioSample.class);
    PlayerSession playerSession = PlayerSession.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (currentLocation == null || status.getStatus() == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Current location is not recorded"));
            return;
        }

        List<CodexEntryEvent> codexEntries = currentLocation.getCodexEntries();
        if (codexEntries == null || codexEntries.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No codex entries found."));
            return;
        }

        double userLongitude = status.getStatus().getLongitude();
        double userLatitude = status.getStatus().getLatitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        List<BioSampleDto> partialBioSamples = currentLocation.getPartialBioSamples();
        boolean hasPartialBioScans = partialBioSamples != null && !partialBioSamples.isEmpty();
        CodexEntryEvent entry;

        if (hasPartialBioScans) {
            entry = findPartialScanMatch(partialBioSamples, codexEntries, userLatitude, userLongitude, planetRadius);
        } else {
            entry = findNearestBioForm(codexEntries, userLatitude, userLongitude, planetRadius, currentLocation.getPlanetName());
        }

        if (entry == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No codex entries found."));
            return;
        }

        double entryLatitude = entry.getLatitude();
        double entryLongitude = entry.getLongitude();

        TargetLocation targetLocation = new TargetLocation();
        targetLocation.setLatitude(entryLatitude);
        targetLocation.setLongitude(entryLongitude);
        targetLocation.setEnabled(true);
        targetLocation.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(targetLocation);

        EventBusManager.publish(new AiVoxResponseEvent("Navigating to: " + entry.getNameLocalised()));
    }

    private  CodexEntryEvent findClosestBiologyEntry(List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius, BioSampleDto bioSample, List<String> filterGenus) {
        final double minimumDistanceBetweenSamples = BioForms.getDistance(bioSample.getGenus());

        for (CodexEntryEvent codexEntry : codexEntries) {
            if(codexEntry.getNameLocalised() == null) continue;
            boolean isBiology = "$Codex_Category_Biology;".equalsIgnoreCase(codexEntry.getCategory());
            boolean matchesGenus = codexEntry.getNameLocalised().startsWith(bioSample.getGenus());
            boolean notFilteredGenus = filterGenus != null && !filterGenus.contains(codexEntry.getNameLocalised().split(" ")[0]);
            if (isBiology && (matchesGenus || notFilteredGenus)) {
                double distanceBetweenPartialSampleAndCodexEntry = calculateSurfaceDistance(bioSample.getScanLatitude(), bioSample.getScanLongitude(), codexEntry.getLatitude(), codexEntry.getLongitude(), planetRadius, 0);
                if(distanceBetweenPartialSampleAndCodexEntry > minimumDistanceBetweenSamples) {
                    return codexEntry;
                }
            }
        }
        return null;
    }
    
    private  CodexEntryEvent findNearestBioForm(List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius, String planetName) {
        List<BioSampleDto> samples = playerSession.getBioCompletedSamples();
        ArrayList<String> filterGenus = new ArrayList<>();
        for (BioSampleDto sample : samples) {
            if(planetName.equalsIgnoreCase(sample.getPlanetName())) {
                String genus = sample.getGenus();
                filterGenus.add(genus);
            }
        }


        return findClosestBiologyEntry(codexEntries, userLatitude, userLongitude, planetRadius, null, filterGenus);
    }

    private  CodexEntryEvent findPartialScanMatch(List<BioSampleDto> partialBioSamples, List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius) {
        for (BioSampleDto partial : partialBioSamples) {
            CodexEntryEvent entry = findClosestBiologyEntry(codexEntries, userLatitude, userLongitude, planetRadius, partial, null);
            if (entry != null) return entry;
        }

        return null;
    }
}