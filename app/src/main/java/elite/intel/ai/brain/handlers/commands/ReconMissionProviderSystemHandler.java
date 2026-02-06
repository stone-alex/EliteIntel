package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateHuntingGroundsDao.HuntingGround;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.HuntingGroundManager.PirateMissionTuple;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class ReconMissionProviderSystemHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final HuntingGroundManager huntingGroundManager = HuntingGroundManager.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    public ReconMissionProviderSystemHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());

        List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> huntingGrounds = huntingGroundManager.findInProviderForTargetStarSystem(currentLocation.getStarName());
        MissionProvider provider = null;
        String targetStarSystemName = "";
        for (PirateMissionTuple<HuntingGround, List<MissionProvider>> pair : huntingGrounds) {
            List<MissionProvider> providers = pair.getMissionProvider();
            provider = providers.stream().filter(p -> p.getMissionProviderFaction() == null).findFirst().orElse(null);
            targetStarSystemName = pair.getTarget().getStarSystem();
            if (provider != null) break;
        }

        if (provider == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No mission providers found."));
            return;
        }


        HuntingGround target = huntingGrounds.stream().filter(
                data -> data.getTarget().getTargetFaction() == null
        ).findFirst().map(PirateMissionTuple::getTarget).orElse(null);


        String starSystem = provider.getStarSystem();
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Plotting route to " + starSystem + " when you get there look for factions targeting " + targetStarSystemName + " at local ports."));
        RoutePlotter plotter = new RoutePlotter(controller);
        plotter.plotRoute(starSystem);
        ReminderManager.getInstance().setDestination(
                new DataDto(
                        starSystem,
                        target == null
                                ? " Seek mission providers in local ports with pirate massacre missions against " + targetStarSystemName + " system."
                                : " Target Faction: " + target.getTargetFaction()).toJson()
        );
    }

    record DataDto(String starSystem, String targetFaction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
