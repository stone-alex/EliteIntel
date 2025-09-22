package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.gameapi.journal.events.LiftoffEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    private static final Logger log = LogManager.getLogger(LiftoffEventSubscriber.class);

    private final KeyBindingsParser parser;
    private final KeyBindingExecutor executor;
    private final BindingsMonitor monitor;

    public LiftoffEventSubscriber() {
        this.parser = KeyBindingsParser.getInstance();
        this.executor = KeyBindingExecutor.getInstance();
        this.monitor = BindingsMonitor.getInstance();
    }

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        boolean isPlayerControlled = event.isPlayerControlled();
        boolean isOnPlanet = event.isOnPlanet();
        PlayerSession playerSession = PlayerSession.getInstance();

        if (isPlayerControlled) {
            String landingGearToggle = GameCommands.GameCommand.LANDING_GEAR_TOGGLE.getGameBinding();
            try {
                Thread.sleep(1500);
                operateKeyboard(landingGearToggle, 0);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    protected void operateKeyboard(String action, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(GameCommands.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
            log.info("Executed action: {} with key: {}", action, binding);
        } else {
            log.warn("No binding found for action: {}", action);
        }
    }

}
