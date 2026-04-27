package elite.intel.ai.hands;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.hands.events.EnterTextEvent;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.hands.events.GameTapEvent;
import elite.intel.ai.hands.events.RawKeyEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.PlayerSession;
import elite.intel.session.ui.UINavigator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.SleepNoThrow.sleep;

/**
 * Sole consumer of {@link GameControllerBus} events.
 * <p>
 * Translates game input events into physical keystrokes via {@link KeyBindingExecutor}
 * and {@link KeyProcessor}. By keeping all keystroke execution here, the rest of the
 * codebase (handlers, RoutePlotter, GameController) only publishes intent - they never
 * touch the keyboard directly.
 */
public class HandsSubscriber {

    private static final Logger log = LogManager.getLogger(HandsSubscriber.class);

    private final BindingsMonitor monitor = BindingsMonitor.getInstance();
    private final KeyBindingExecutor executor = KeyBindingExecutor.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    public HandsSubscriber() {
        GameControllerBus.register(this);
    }

    @Subscribe
    public void onGameInput(GameInputEvent event) {
        if (monitor.getBindings() == null) return;
        if (playerSession.useVm()) return;
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(event.getBindingId());
        if (binding != null) {
            executor.executeBindingWithHold(binding, event.getHoldTime());
        } else {
            log.warn("No binding found for action: {}", event.getBindingId());
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(
                    "No key binding found for " + event.getBindingId())
            );
        }
        sleep(UINavigator.randomDelay());
    }

    @Subscribe
    public void onGameTap(GameTapEvent event) {
        if (monitor.getBindings() == null) return;
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(event.getBindingId());
        if (binding != null) {
            log.debug("Tap binding: key={}, ignoring hold flag={}", binding.key, binding.hold);
            executor.executeTap(binding);
        } else {
            log.warn("No binding found for action: {}", event.getBindingId());
            EventBusManager.publish(new MissionCriticalAnnouncementEvent(
                    "No key binding found for " + event.getBindingId()));
        }
        sleep(UINavigator.randomDelay());
    }

    @Subscribe
    public void onEnterText(EnterTextEvent event) {
        KeyProcessor.getInstance().enterText(event.getText());
    }

    @Subscribe
    public void onRawKey(RawKeyEvent event) {
        KeyProcessor.getInstance().pressKey(event.getKeyCode());
    }
}
