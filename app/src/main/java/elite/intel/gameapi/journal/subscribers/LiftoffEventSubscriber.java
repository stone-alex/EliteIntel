package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.gameapi.journal.events.LiftoffEvent;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_LANDING_GEAR_TOGGLE;

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    private final GlobalSettingsManager globalSettingsManager = GlobalSettingsManager.getInstance();

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        if (globalSettingsManager.getAutoGearUpOnTakeOff()) {
            Thread.ofVirtual().start(() -> {
                Status status = Status.getInstance();
                if (status.isInMainShip() && status.isLandingGearDown()) {
                    GameControllerBus.publish(new GameInputEvent(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0));
                }
            });
        }
    }
}
