package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.BioSamplesManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;

public class NavigateToNextCodexEntry implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final BioSamplesManager bioSamplesManager = BioSamplesManager.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (currentLocation == null || status.getStatus() == null) {
            EventBusManager.publish(new AiVoxResponseEvent("I don't know where you are yet."));
            return;
        }


        List<CodexEntryEvent> codexEntries = getCodexEntries(currentLocation);
        if (codexEntries == null || codexEntries.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No codex data on this body."));
            return;
        }

        double planetRadius = status.getStatus().getPlanetRadius();
        double playerLat = status.getStatus().getLatitude();
        double playerLon = status.getStatus().getLongitude();

        CodexEntryEvent target = findBestBioTarget(codexEntries, currentLocation.getPartialBioSamples(),
                playerLat, playerLon, planetRadius);

        if (target == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Nothing biological left to scan here."));
            return;
        }

        TargetLocation nav = new TargetLocation();
        nav.setLatitude(target.getLatitude());
        nav.setLongitude(target.getLongitude());
        nav.setEnabled(true);
        nav.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(nav);

        EventBusManager.publish(new AiVoxResponseEvent(
                "Heading to " + target.getNameLocalised() + " sample."));
    }

    private List<CodexEntryEvent> getCodexEntries(LocationDto currentLocation) {
        List<BioSampleDto> completedBioSamples = bioSamplesManager.findByPlanetName(currentLocation.getPlanetName());
        List<CodexEntryEvent> codexEntries = currentLocation.getCodexEntries();
        List<CodexEntryEvent> filteredResult = new ArrayList<>();

        if (completedBioSamples == null || completedBioSamples.isEmpty()) return codexEntries;

        for (CodexEntryEvent entry : codexEntries) {
            for (BioSampleDto partial : completedBioSamples) {
                if (!entry.getNameLocalised().contains(partial.getGenus())) {
                    filteredResult.add(entry);
                }
            }
        }

        return filteredResult;
    }


    private CodexEntryEvent findBestBioTarget(List<CodexEntryEvent> codexEntries,
                                              List<BioSampleDto> partials,
                                              double playerLat, double playerLon, double planetRadius) {

        boolean hasPartials = partials != null && !partials.isEmpty();

        CodexEntryEvent bestPartialMatch = null;
        CodexEntryEvent bestAny = null;
        double bestPartialDist = Double.MAX_VALUE;
        double bestAnyDist = Double.MAX_VALUE;

        for (CodexEntryEvent entry : codexEntries) {
            if (!"$Codex_Category_Biology;".equalsIgnoreCase(entry.getCategory())) continue;
            if (entry.getLatitude() == 0 && entry.getLongitude() == 0) continue;

            String genus = entry.getNameLocalised().split(" ")[0];
            double distToPlayer = calculateSurfaceDistance(playerLat, playerLon,
                    entry.getLatitude(), entry.getLongitude(), planetRadius, 0);

            // Check if this entry is valid (not too close to any partial of same genus)
            boolean valid = hasPartials ? !isTooCloseToAnyPartialOfSameGenus(entry, genus, partials, planetRadius) : true;

            if (!valid) continue;

            // If we have partials and this matches one of their genera → priority track
            if (hasPartials && partials.stream().anyMatch(p -> genus.equalsIgnoreCase(p.getGenus()))) {
                if (distToPlayer < bestPartialDist) {
                    bestPartialDist = distToPlayer;
                    bestPartialMatch = entry;
                }
            }

            // Always track best valid of any genus (fallback)
            if (distToPlayer < bestAnyDist) {
                bestAnyDist = distToPlayer;
                bestAny = entry;
            }
        }

        // Priority: return partial genus match if available, otherwise any valid bio
        return bestPartialMatch != null ? bestPartialMatch : bestAny;
    }

    private boolean isTooCloseToAnyPartialOfSameGenus(CodexEntryEvent entry, String genus,
                                                      List<BioSampleDto> partials, double planetRadius) {
        double minAllowed = BioForms.getDistance(genus); // genus-specific min distance

        for (BioSampleDto partial : partials) {
            if (!genus.equalsIgnoreCase(partial.getGenus())) continue;

            double dist = calculateSurfaceDistance(
                    partial.getScanLatitude(), partial.getScanLongitude(),
                    entry.getLatitude(), entry.getLongitude(), planetRadius, 0);

            if (dist <= minAllowed) {
                return true; // violates rule → discard this codex entry
            }
        }
        return false;
    }
}