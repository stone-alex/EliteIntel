package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.BioSampleDistanceCalculator;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.List;

public class BioSampleTrackingSubscriber {

    @Subscribe
    public void onPlayerMovedEvent(PlayerMovedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        List<BioSampleDto> bioSamples = playerSession.getCurrentLocation().getPartialBioSamples();

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

        BioSampleDto bioSampleDto = bioSamples.getFirst();
        playerSession.getCurrentLocation().setPartialBioSamples(temp);

        // Announce only on state transition
        if (wasFarEnough != isFarEnough) {
            if (isFarEnough) {
                EventBusManager.publish(new SensorDataEvent("Far enough to take the next bio sample."));
            } else {
                EventBusManager.publish(new SensorDataEvent("Too close to the previous bio sample."));
            }
        }
    }
}
