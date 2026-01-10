package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.PirateMissionDataManager;

public class PlotRouteToNextMissionDestination extends CommandOperator implements CommandHandler {

    private GameController controller;
    private final PirateMissionDataManager missionDataManager = PirateMissionDataManager.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    public PlotRouteToNextMissionDestination(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {

    }
}
