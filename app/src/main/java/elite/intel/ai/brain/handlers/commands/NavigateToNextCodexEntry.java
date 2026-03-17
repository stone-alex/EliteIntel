package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.CodexEntryDao;
import elite.intel.db.managers.BioSamplesManager;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static elite.intel.util.NavigationUtils.calculateSurfaceDistance;

public class NavigateToNextCodexEntry implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
    private final BioSamplesManager bioSamplesManager = BioSamplesManager.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());

        if (currentLocation == null || status.getStatus() == null) {
            EventBusManager.publish(new AiVoxResponseEvent("I don't know where you are yet."));
            return;
        }


        List<CodexEntryDao.CodexEntry> codexEntries = getCodexEntries(currentLocation);
        if (codexEntries.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No codex entries found."));
            return;
        }

        double planetRadius = status.getStatus().getPlanetRadius();
        double playerLat = status.getStatus().getLatitude();
        double playerLon = status.getStatus().getLongitude();

        Tuple<CodexEntryDao.CodexEntry, String> target = findBestBioTarget(codexEntries, currentLocation.getPartialBioSamples(), playerLat, playerLon, planetRadius);

        if (target.getSample() == null) {
            EventBusManager.publish(new AiVoxResponseEvent(target.getNote()));
            return;
        }

        TargetLocation nav = new TargetLocation();
        nav.setLatitude(target.getSample().getLatitude());
        nav.setLongitude(target.getSample().getLongitude());
        nav.setEnabled(true);
        nav.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(nav);

        EventBusManager.publish(new AiVoxResponseEvent("Heading to " + target.getSample().getEntryName() + " sample."));
    }

    private List<CodexEntryDao.CodexEntry> getCodexEntries(LocationDto currentLocation) {
        List<BioSampleDto> completedBioSamples = bioSamplesManager.findByPlanetName(currentLocation.getStarName(), currentLocation.getPlanetName());
        List<CodexEntryDao.CodexEntry> codexEntries = codexEntryManager.getForPlanet(currentLocation.getStarName(), currentLocation.getBodyId());
        if (codexEntries == null) {
            return new ArrayList<>();
        }
        List<CodexEntryDao.CodexEntry> filteredResult = new ArrayList<>();
        if (completedBioSamples == null || completedBioSamples.isEmpty()) return codexEntries;

        List<BioSampleDto> partialBioSamples = currentLocation.getPartialBioSamples();

        if (!partialBioSamples.isEmpty()) {
            for (CodexEntryDao.CodexEntry entry : codexEntries) {
                for (BioSampleDto partial : partialBioSamples) {
                    if (entry.getEntryName().toLowerCase(Locale.ROOT).contains(partial.getGenus().toLowerCase(Locale.ROOT))) {
                        filteredResult.add(entry);
                        return filteredResult;
                    }
                }
            }
            return new ArrayList<>();
        }

        for (CodexEntryDao.CodexEntry entry : codexEntries) {
            for (BioSampleDto completed : completedBioSamples) {
                if (!entry.getEntryName().contains(completed.getGenus())) {
                    filteredResult.add(entry);
                }
            }
        }

        return filteredResult;
    }


    private Tuple<CodexEntryDao.CodexEntry, String> findBestBioTarget(List<CodexEntryDao.CodexEntry> codexEntries, List<BioSampleDto> partials, double playerLat, double playerLon, double planetRadius) {
        String partialGenus = playerSession.getCurrentPartial();
        boolean hasPartials = partialGenus != null && !partials.isEmpty();
        String message = "";
        CodexEntryDao.CodexEntry bestPartialMatch = null;
        CodexEntryDao.CodexEntry bestAny = null;
        double bestPartialDist = Double.MAX_VALUE;
        double bestAnyDist = Double.MAX_VALUE;
        boolean isTooClose = false;
        for (CodexEntryDao.CodexEntry entry : codexEntries) {
            if (entry.getLatitude() == 0 && entry.getLongitude() == 0) continue;

            String genus = entry.getEntryName().split(" ")[0];
            double distToPlayer = calculateSurfaceDistance(playerLat, playerLon, entry.getLatitude(), entry.getLongitude(), planetRadius, 0);

            // Check distance (no partials or not too close to any partial of the same genus)
            isTooClose = isTooCloseToAnyPartialOfSameGenus(entry, genus, partials, planetRadius);
            boolean valid = !hasPartials || !isTooClose;
            if (!valid) continue;

            BioSampleDto partialMatch = findForGenus(genus, partials);
            Integer scanXof3 = partialMatch == null ? null : partialMatch.getScanXof3();

            if (hasPartials && scanXof3 != null && scanXof3 > 2) {
                // scans completed, skip
                continue;
            }

            if (partialMatch != null && !partialMatch.getGenus().equalsIgnoreCase(partialGenus)) {
                continue;
            }

            // If we have partials and this matches one of their genera → priority track
            if (hasPartials && partials.stream().anyMatch(p -> genus.equalsIgnoreCase(p.getGenus()) && genus.equalsIgnoreCase(partialGenus))) {
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

        CodexEntryDao.CodexEntry entry = bestPartialMatch != null ? bestPartialMatch : bestAny;
        if (entry == null && isTooClose) message = "No matches found for partial sample within range.";
        return new Tuple<>(entry, message);
    }

    private BioSampleDto findForGenus(String genus, List<BioSampleDto> partials) {
        if (partials.isEmpty()) return null;
        return partials.stream()
                .filter(p -> genus.equalsIgnoreCase(p.getGenus()))
                .findFirst()
                .orElse(null);
    }

    private boolean isTooCloseToAnyPartialOfSameGenus(CodexEntryDao.CodexEntry entry, String genus, List<BioSampleDto> partials, double planetRadius) {
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

    class Tuple<S, N> {
        private final S sample;
        private final N note;

        Tuple(S sample, N note) {
            this.sample = sample;
            this.note = note;
        }

        public S getSample() {
            return sample;
        }

        public N getNote() {
            return note;
        }
    }
}