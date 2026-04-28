package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.gameapi.gamestate.status_events.BeingInterdictedEvent;

import static elite.intel.ai.brain.actions.Commands.ACTIVATE_COMBAT_MODE;
import static elite.intel.ai.brain.actions.Commands.SELECT_HIGHEST_THREAT;

public class InterdictionHandler {

    private final CommandHandlerFactory commandHandlerFactory = CommandHandlerFactory.getInstance();

    @Subscribe
    public void onInterdictedEvent(BeingInterdictedEvent event) {
        CommandHandler activateCombatMode = commandHandlerFactory.getCommandHandlers().get(ACTIVATE_COMBAT_MODE.getAction());
        if (activateCombatMode != null)
            new Thread(() -> activateCombatMode.handle(ACTIVATE_COMBAT_MODE.getAction(), null, "")).start();

        CommandHandler handler = commandHandlerFactory.getCommandHandlers().get(SELECT_HIGHEST_THREAT.getAction());
        if (handler != null)
            new Thread(() -> handler.handle(SELECT_HIGHEST_THREAT.getAction(), null, "")).start();
    }
}
