package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.MissionDto;

import java.util.Collection;
import java.util.Optional;

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
        Collection<MissionDto> missions = missionManager.getMissions(missionManager.getPirateMissionTypes()).values();
        if(missions.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No missions found."));
        }

        Optional<MissionDto> firstMission = missions.stream().findFirst();
        if(!firstMission.isPresent()) {
            EventBusManager.publish(new AiVoxResponseEvent("No missions found."));
        } else {
            MissionDto mission = firstMission.get();
            EventBusManager.publish(new AiVoxResponseEvent("Head to " + mission.getDestinationSystem() + " system."));
            RoutePlotter plotter = new RoutePlotter(this.controller);
            plotter.plotRoute(mission.getDestinationSystem());
        }
    }
}
