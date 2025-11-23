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

import java.util.List;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;

public class NavigateToNextBioSample implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(NavigateToNextBioSample.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (currentLocation == null || status.getStatus() == null) {
            EventBusManager.publish(new AiVoxResponseEvent("I don't know where you are yet."));
            return;
        }

        List<CodexEntryEvent> codexEntries = currentLocation.getCodexEntries();
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
    private CodexEntryEvent findBestBioTarget(List<CodexEntryEvent> codexEntries,
                                              List<BioSampleDto> partials,
                                              double playerLat, double playerLon, double planetRadius) {

        boolean hasPartials = partials != null && !partials.isEmpty();

        CodexEntryEvent best = null;
        double bestDistanceToPlayer = Double.MAX_VALUE;

        for (CodexEntryEvent entry : codexEntries) {
            if (!"$Codex_Category_Biology;".equalsIgnoreCase(entry.getCategory())) continue;
            if (entry.getLatitude() == 0 && entry.getLongitude() == 0) continue;

            String entryGenus = entry.getNameLocalised().split(" ")[0];

            // RULE 1: Hard filter — reject any codex entry too close to a partial of same genus
            if (hasPartials && isTooCloseToAnyPartialOfSameGenus(entry, entryGenus, partials, planetRadius)) {
                continue; // completely invalid target
            }

            // If we get here: entry is valid → compare distance to player
            double distToPlayer = calculateSurfaceDistance(playerLat, playerLon,
                    entry.getLatitude(), entry.getLongitude(), planetRadius, 0);

            if (distToPlayer < bestDistanceToPlayer) {
                bestDistanceToPlayer = distToPlayer;
                best = entry;
            }
        }

        return best;
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