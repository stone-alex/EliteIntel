package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.MissionDto;

public class PlotRouteToMissionDestination extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final MissionManager missionManager = MissionManager.getInstance();

    public PlotRouteToMissionDestination(GameController controller) {
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
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("No missions found."));
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Head to ");
        if(mission.getDestinationSystem() != null){
            sb.append(mission.getDestinationStation());
        }
        if(mission.getDestinationSettlement() != null){
            sb.append(mission.getDestinationStation());
        }

        ReminderManager.getInstance().setDestination(
                sb.toString()
        );

        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Head to " + mission.getDestinationSystem() + " system."));
        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(mission.getDestinationSystem());
    }
}