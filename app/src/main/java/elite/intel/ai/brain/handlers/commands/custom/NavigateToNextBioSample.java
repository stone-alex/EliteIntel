package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
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
import java.util.Optional;

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

        List<CodexEntryEvent> codexEntries = playerSession.getCurrentLocation().getCodexEntries();
        if (codexEntries == null || codexEntries.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No codex entries found."));
            return;
        }

        double userLongitude = status.getStatus().getLongitude();
        double userLatitude = status.getStatus().getLatitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        List<BioSampleDto> bioScans = currentLocation.getPartialBioSamples();
        boolean hasPartialBioScans = bioScans != null && !bioScans.isEmpty();
        CodexEntryEvent entry;

        if (hasPartialBioScans) {
            entry = findPartialScanMatch(bioScans, codexEntries, userLatitude, userLongitude, planetRadius);
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

    private  CodexEntryEvent findClosestBiologyEntry(List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius, String matchGenus, List<String> filterGenus) {
        CodexEntryEvent closestEntry = null;
        double minDistance = Double.MAX_VALUE;
        for (CodexEntryEvent e : codexEntries) {
            if(e.getNameLocalised() == null) continue;
            boolean isBiology = "$Codex_Category_Biology;".equalsIgnoreCase(e.getCategory());
            boolean matchesGenus = matchGenus != null && e.getNameLocalised().startsWith(matchGenus);
            boolean notFilteredGenus = filterGenus != null && !filterGenus.contains(e.getNameLocalised().split(" ")[0]);
            if (isBiology && (matchesGenus || notFilteredGenus)) {
                double entryLat = e.getLatitude();
                double entryLon = e.getLongitude();
                double distance = calculateSurfaceDistance(userLatitude, userLongitude, entryLat, entryLon, planetRadius, 0);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestEntry = e;
                }
            }
        }
        return closestEntry;
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

    private  CodexEntryEvent findPartialScanMatch(List<BioSampleDto> bioScans, List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius) {
        Optional<BioSampleDto> bioSampleDto = bioScans.stream().findFirst();
        String match = bioSampleDto.get().getGenus();
        return findClosestBiologyEntry(codexEntries, userLatitude, userLongitude, planetRadius, match, null);
    }
}