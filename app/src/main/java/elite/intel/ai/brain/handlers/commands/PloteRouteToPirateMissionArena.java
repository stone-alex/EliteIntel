package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.gameapi.EventBusManager;

import java.util.Optional;

public class PloteRouteToPirateMissionArena extends CommandOperator implements CommandHandler {


    private final HuntingGroundManager missionDataManager = HuntingGroundManager.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();
    private final GameController controller;

    public PloteRouteToPirateMissionArena(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }


    @Override public void handle(String action, JsonObject params, String responseText) {
        Optional<String> firstFaction = missionManager.getTargetFactions(
                missionManager.getPirateMissionTypes()
        ).stream().findFirst();

        String factionName = firstFaction.orElse(null);
        if (factionName == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No factions found"));
            return;
        }
        String starSystemForFactionName = missionDataManager.findStarSystemForFactionName(factionName);

        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Plotting route to " + starSystemForFactionName));

        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(starSystemForFactionName);

        ReminderManager.getInstance().setDestination(
                starSystemForFactionName
        );
    }
}
