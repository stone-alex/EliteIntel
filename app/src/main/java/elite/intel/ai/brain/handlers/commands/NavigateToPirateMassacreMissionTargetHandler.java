package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.MissionDto;


import java.util.Set;
import java.util.List;

public class NavigateToPirateMassacreMissionTargetHandler extends CommandOperator implements CommandHandler {

    private GameController controller;

    public NavigateToPirateMassacreMissionTargetHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        MissionManager missionManager = MissionManager.getInstance();

        List<String> missionTypes = missionManager.getPirateMissionTypes();
        //list contains only missions that are not completed;
        Set<String> targetFactions = missionManager.getTargetFactions(missionTypes);

        if (targetFactions.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No pirate massacre mission providers found."));
            return;
        }



        MissionDto mission = missionManager.getMissions(missionTypes).values().stream().filter(
                v -> v.getMissionType().equals("PirateMassacre")
        ).findFirst().orElse(null);

        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(mission.getDestinationSystem());
    }
}
