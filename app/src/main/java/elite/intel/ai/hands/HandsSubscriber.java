package elite.intel.ai.hands;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.hands.events.EnterTextEvent;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.hands.events.GameTapEvent;
import elite.intel.ai.hands.events.RawKeyEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.ui.UINavigator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.util.SleepNoThrow.sleep;

/**
 * Sole consumer of {@link GameControllerBus} events.
 * <p>
 * Translates game input events into physical keystrokes via {@link KeyBindingExecutor}
 * and {@link KeyProcessor}. By keeping all keystroke execution here, the rest of the
 * codebase (handlers, RoutePlotter, HandsService) only publishes intent - they never
 * touch the keyboard directly.
 */
public class HandsSubscriber {

    private static final Logger log = LogManager.getLogger(HandsSubscriber.class);

    private final BindingsMonitor monitor = BindingsMonitor.getInstance();
    private final KeyBindingExecutor executor = KeyBindingExecutor.getInstance();

    public HandsSubscriber() {
        GameControllerBus.register(this);
    }

    @Subscribe
    public void onGameInput(GameInputEvent event) {
        if (monitor.getBindings() == null) {
            log.warn("[key] bindings not loaded - dropping {}", event.getBindingId());
            return;
        }
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(event.getBindingId());
        if (binding != null) {
            log.info("[key] action=[{}] key=[{}] modifiers={} hold={}ms",
                    event.getBindingId(), binding.key, binding.modifiers, event.getHoldTime());
            executor.executeBindingWithHold(binding, event.getHoldTime());
        } else {
            log.warn("[key] NO BINDING for action=[{}]", event.getBindingId());
            handleNoKeyBindingFound(event.getBindingId());
        }
        sleep(UINavigator.randomDelay());
    }

    @Subscribe
    public void onGameTap(GameTapEvent event) {
        if (monitor.getBindings() == null) return;
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(event.getBindingId());
        if (binding != null) {
            log.info("[key] tap action=[{}] key=[{}]", event.getBindingId(), binding.key);
            executor.executeTap(binding);
        } else {
            log.warn("[key] NO BINDING for tap action=[{}]", event.getBindingId());
            handleNoKeyBindingFound(event.getBindingId());
        }
        sleep(UINavigator.randomDelay());
    }

    private void handleNoKeyBindingFound(String event) {
        log.warn("No binding found for action: {}", event);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(
                "No key binding found for " + event)
        );
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
