package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.gameapi.journal.events.LaunchSRVEvent;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

public class LaunchSRVSubscriber {

    private final Status status = Status.getInstance();

    @Subscribe
    public void onLaunchSRVEvent(LaunchSRVEvent event) {
        Thread.ofVirtual().start(() -> {
            SleepNoThrow.sleep(6000);

            ///
            if (status.isSrvHighBeam() || status.isLightsOn()) {
                if (status.isSrvHighBeam()) {
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                } else {
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                    toggleLights(Bindings.GameCommand.BINDING_BUGGY_LIGHTS_TOGGLE.getGameBinding());
                }
            }
        });
    }

    private void toggleLights(String binding) {
        GameControllerBus.publish(new GameInputEvent(binding, 0));
    }
}
