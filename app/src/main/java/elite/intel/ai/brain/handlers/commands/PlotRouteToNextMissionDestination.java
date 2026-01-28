package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.MissionDto;

public class PlotRouteToNextMissionDestination extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final MissionManager missionManager = MissionManager.getInstance();

    public PlotRouteToNextMissionDestination(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        String keyword = params.get("key") == null ? null : params.get("key").getAsString();
        MissionDto mission = missionManager.findByKeyword(keyword).stream().findFirst().orElse(null);
        if (mission == null) {
            mission = missionManager.getMissions().values().stream().findFirst().orElse(null);
            if (mission == null) {
                EventBusManager.publish(new AiVoxResponseEvent("No missions found."));
                return;
            }
        }

        EventBusManager.publish(new AiVoxResponseEvent("Head to " + mission.getDestinationSystem() + " system."));
        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(mission.getDestinationSystem());
    }
}