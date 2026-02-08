package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;

import java.util.Collection;
import java.util.List;

public class PlotRouteToKnownPirateMassacreMissionProvider extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final HuntingGroundManager huntingGroundManager = HuntingGroundManager.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    public PlotRouteToKnownPirateMassacreMissionProvider(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        List<MissionProvider> missionProviders = huntingGroundManager.findConfirmedMissionProviders();
        Collection<MissionDto> values = missionManager.getMissions().values();
        String destination = null;
        String targetSystem = null;
        for (MissionProvider provider : missionProviders) {
            if (!missionManager.getMissions().isEmpty()) {
                for (MissionDto mission : values) {
                    if (mission.getFaction().equalsIgnoreCase(provider.getMissionProviderFaction())) {
                        destination = provider.getStarSystem();
                        targetSystem = provider.getTargetSystem();
                        break;
                    }
                }
            } else {
                destination = provider.getStarSystem();
                targetSystem = provider.getTargetSystem();
                break;
            }
        }

        if (destination == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Knowing mission providers. Searching..."));
            EventBusManager.publish(new UserInputEvent(" find hunting grounds", 100f));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Head to " + destination + " look for factions with missions against " + targetSystem + " system."));
            RoutePlotter plotter = new RoutePlotter(this.controller);
            plotter.plotRoute(destination);
        }
    }
}
