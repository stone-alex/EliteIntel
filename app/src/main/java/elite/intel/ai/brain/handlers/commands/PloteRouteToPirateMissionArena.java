package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.gameapi.EventBusManager;

import java.util.Optional;

public class PloteRouteToPirateMissionArena extends CommandOperator implements CommandHandler {


    private GameController controller;
    private final PirateMissionDataManager missionDataManager = PirateMissionDataManager.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    public PloteRouteToPirateMissionArena(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }


    @Override public void handle(String action, JsonObject params, String responseText) {
        Optional<String> firstFaction = missionManager.getTargetFactions(
                missionManager.getPirateMissionTypes()
        ).stream().findFirst();

        String factionName = firstFaction.get();
        if(factionName == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No factions found"));
            return;
        }
        String starSystemForFactionName = missionDataManager.findStarSystemForFactionName(factionName);

        EventBusManager.publish(new AiVoxResponseEvent("Plotting route to " + starSystemForFactionName));

        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(starSystemForFactionName);
        DestinationReminderManager.getInstance().setDestination(
                new ReconPirateMissionTargetSystemHandler.DataDto(starSystemForFactionName).toJson()
        );

    }
}
