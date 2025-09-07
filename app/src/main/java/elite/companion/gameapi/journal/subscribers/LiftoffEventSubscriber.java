package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.brain.robot.BindingsMonitor;
import elite.companion.comms.brain.robot.KeyBindingExecutor;
import elite.companion.comms.brain.robot.KeyBindingsParser;
import elite.companion.comms.handlers.command.CommandActionsGame;
import elite.companion.gameapi.journal.events.LiftoffEvent;
import elite.companion.session.PlayerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(LiftoffEventSubscriber.class);

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
        PlayerSession.getInstance().remove(PlayerSession.STATION_DATA);

        if (isPlayerControlled) {
            String landingGearToggle = CommandActionsGame.GameCommand.LANDING_GEAR_TOGGLE.getGameBinding();
            try {
                Thread.sleep(1500);
                operateKeyboard(landingGearToggle, 0);
            } catch (InterruptedException e) {
                //oh well WE GOT INTERRUPTED! So what...
            }
        }
    }

    protected void operateKeyboard(String action, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(CommandActionsGame.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
            log.info("Executed action: {} with key: {}", action, binding);
        } else {
            log.warn("No binding found for action: {}", action);
        }
    }

}
