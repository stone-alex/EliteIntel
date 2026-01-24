package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.status_events.PlayerMovedEvent;
import elite.intel.gameapi.journal.BioSampleDistanceCalculator;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.List;

public class BioSampleTrackingSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final Status status = Status.getInstance();

    @Subscribe
    public void onPlayerMovedEvent(PlayerMovedEvent event) {
        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<BioSampleDto> bioSamples = currentLocation.getPartialBioSamples();

        // If no samples are being tracked, don't announce.
        if (bioSamples == null || bioSamples.isEmpty()) {
            return;
        }

        // Compute previous aggregate state before updating flags.
        boolean wasFarEnough = bioSamples.stream().allMatch(BioSampleDto::isPlayerFarEnough);

        List<BioSampleDto> temp = new ArrayList<>();
        boolean isFarEnough = true;

        for (BioSampleDto bioSample : bioSamples) {
            boolean canTakeSample = BioSampleDistanceCalculator.isFarEnoughFromSample(
                    bioSample.getGenus(),
                    bioSample.getSpecies(),
                    bioSample.getScanLatitude(),
                    bioSample.getScanLongitude(),
                    status.getStatus().getLatitude(),
                    status.getStatus().getLongitude(),
                    status.getStatus().getPlanetRadius()
            );

            bioSample.setPlayerFarEnough(canTakeSample);
            temp.add(bioSample);

            if (!canTakeSample) {
                isFarEnough = false;
            }
        }

        currentLocation.setPartialBioSamples(temp);
        playerSession.saveLocation(currentLocation);

        // Announce only on state transition
        if (wasFarEnough != isFarEnough) {
            if (isFarEnough) {
                EventBusManager.publish(new SensorDataEvent("You Are now far enough to take the new sample.", "Notify User."));
            } else {
                EventBusManager.publish(new SensorDataEvent("You Are moved too close to previous colony to take new sample.", "Warn User."));
            }
        }
    }
}
