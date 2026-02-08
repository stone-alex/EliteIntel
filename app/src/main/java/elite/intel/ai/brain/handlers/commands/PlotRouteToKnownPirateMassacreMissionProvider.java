package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

import java.util.Collection;
import java.util.List;

public class PlotRouteToKnownPirateMassacreMissionProvider extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final HuntingGroundManager huntingGroundManager = HuntingGroundManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    public PlotRouteToKnownPirateMassacreMissionProvider(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        LocationDto location = locationManager.findByLocationData(playerSession.getLocationData());
        List<MissionProvider> missionProviders = huntingGroundManager.findConfirmedMissionProviders();
        String destination = null;
        String targetSystem = null;
        for (MissionProvider provider : missionProviders) {
            if (!location.getStarName().equalsIgnoreCase(provider.getStarSystem())){
                destination = provider.getStarSystem();
                targetSystem = provider.getTargetSystem();
                break;
            }
        }

        if (location.getStarName().equalsIgnoreCase(targetSystem)){
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Check ports and outposts around this star. Look for factions with missions against " + targetSystem + " star system."));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Head to " + destination + " look for factions with missions against " + targetSystem + " system."));
        }

        if (destination == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Knowing mission providers. Searching..."));
            EventBusManager.publish(new UserInputEvent(" find hunting grounds", 100f));
        } else {
            RoutePlotter plotter = new RoutePlotter(this.controller);
            plotter.plotRoute(destination);
        }
    }
}
