package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.db.managers.PirateMissionDataManager.PirateMissionTuple;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class ReconMissionProviderSystemHandler extends CommandOperator implements CommandHandler {

    private GameController controller;

    public ReconMissionProviderSystemHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        PirateMissionDataManager manager = PirateMissionDataManager.getInstance();
        LocationManager locationManager = LocationManager.getInstance();
        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> huntingGrounds = manager.findInRangeForRecon(locationManager.getGalacticCoordinates(), 100);

        MissionProvider provider = null;
        String targetStarSystemName = "";
        for (PirateMissionTuple<PirateFaction, List<MissionProvider>> pair : huntingGrounds) {
            List<MissionProvider> providers = pair.getMissionProvider();
            provider = providers.stream().filter(p -> p.getMissionProviderFaction() == null).findFirst().orElse(null);
            targetStarSystemName = pair.getTarget().getStarSystem();
            if (provider != null) break;
        }

        if (provider == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No mission providers found."));
            return;
        }


        PirateFaction target = huntingGrounds.stream().filter(
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
