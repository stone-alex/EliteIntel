package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.brain.robot.BindingsMonitor;
import elite.companion.comms.brain.robot.KeyBindingExecutor;
import elite.companion.comms.brain.robot.KeyBindingsParser;
import elite.companion.comms.handlers.command.CommandActionsGame;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.LiftoffEvent;
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
        String liftoffType = isPlayerControlled ? "Manual" : "Unmanned";
        String liftoffFromType = isOnPlanet ? "Planet" : "Station";
        String localStarSystem = event.getStarSystem();
        String localBody = event.getBody();

        StringBuilder sb = new StringBuilder();
        sb.append("Liftoff: ");
        sb.append(" Type:");
        sb.append(liftoffType);
        sb.append(", From:");
        sb.append(liftoffFromType);
        sb.append(", ");
        sb.append(localBody);
        sb.append(".");
        if (isPlayerControlled && isOnPlanet) {
            String landingGearToggle = CommandActionsGame.GameCommand.LANDING_GEAR_TOGGLE.getGameBinding();
            operateKeyboard(landingGearToggle, 0);
        }
        ;

        EventBusManager.publish(new SensorDataEvent(sb.toString()));
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
            EventBusManager.publish(new VoiceProcessEvent("Custom command operator. No key binding found for action " + action));
        }
    }

}
