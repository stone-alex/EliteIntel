package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;

public class NavigateToNextBioSample implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(NavigateToNextBioSample.class);

    @Override
    public void handle(JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (currentLocation == null || playerSession.getStatus() == null) {
            EventBusManager.publish(new VoiceProcessEvent("Current location is not recorded"));
            return;
        }

        List<CodexEntryEvent> codexEntries = playerSession.getCodexEntries();
        if (codexEntries == null || codexEntries.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("No codex entries found."));
            return;
        }

        double userLongitude = playerSession.getStatus().getLongitude();
        double userLatitude = playerSession.getStatus().getLatitude();
        double planetRadius = playerSession.getStatus().getPlanetRadius();

        List<BioSampleDto> bioScans = currentLocation.getBioScans();
        boolean hasPartialBioScans = bioScans != null && !bioScans.isEmpty();
        CodexEntryEvent entry;

        if (hasPartialBioScans) {
            entry = findPartialScanMatch(bioScans, codexEntries, userLatitude, userLongitude, planetRadius);
        } else {
            entry = findNearestBioForm(codexEntries, userLatitude, userLongitude, planetRadius);
        }

        if (entry == null) {
            EventBusManager.publish(new VoiceProcessEvent("No codex entries found."));
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

        EventBusManager.publish(new VoiceProcessEvent("Directions to: " + entry.getNameLocalised()));
    }

    private static CodexEntryEvent findClosestBiologyEntry(List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius, String genusFilter) {
        CodexEntryEvent closestEntry = null;
        double minDistance = Double.MAX_VALUE;
        for (CodexEntryEvent e : codexEntries) {
            if ("$Codex_Category_Biology;".equalsIgnoreCase(e.getCategory()) && (genusFilter == null || e.getNameLocalised().startsWith(genusFilter))) {
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

    private static CodexEntryEvent findNearestBioForm(List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius) {
        return findClosestBiologyEntry(codexEntries, userLatitude, userLongitude, planetRadius, null);
    }

    private static CodexEntryEvent findPartialScanMatch(List<BioSampleDto> bioScans, List<CodexEntryEvent> codexEntries, double userLatitude, double userLongitude, double planetRadius) {
        Optional<BioSampleDto> bioSampleDto = bioScans.stream().findFirst();
        String genus = bioSampleDto.get().getGenus();
        return findClosestBiologyEntry(codexEntries, userLatitude, userLongitude, planetRadius, genus);
    }
}