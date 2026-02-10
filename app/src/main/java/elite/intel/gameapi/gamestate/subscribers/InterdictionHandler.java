package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.ai.hands.GameController;
import elite.intel.gameapi.gamestate.status_events.BeingInterdictedEvent;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_COMBAT_MODE;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SELECT_HIGHEST_THREAT;

public class InterdictionHandler extends CommandOperator {


    private final Status status = Status.getInstance();

    public InterdictionHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Subscribe
    public void onInterdictedEvent(BeingInterdictedEvent event) {
        boolean analysisMode = status.isAnalysisMode();
        if (analysisMode) {
            operateKeyboard(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            operateKeyboard(BINDING_SELECT_HIGHEST_THREAT.getGameBinding(), 0);
        }
    }
}
