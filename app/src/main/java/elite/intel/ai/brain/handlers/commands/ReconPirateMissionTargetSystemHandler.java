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

public class ReconPirateMissionTargetSystemHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;

    public ReconPirateMissionTargetSystemHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        PirateMissionDataManager manager = PirateMissionDataManager.getInstance();
        LocationManager locationManager = LocationManager.getInstance();
        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> huntingGrounds = manager.findInRangeForRecon(locationManager.getGalacticCoordinates(), 100);

        PirateFaction target = huntingGrounds.stream().filter(
                data -> data.getTarget().getTargetFaction() == null
        ).findFirst().map(PirateMissionTuple::getTarget).orElse(null);

        if (target == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No target systems found."));
            return;
        }

        String starSystem = target.getStarSystem();
        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(starSystem);
        EventBusManager.publish(
                new MissionCriticalAnnouncementEvent(
                        "Plotting route to target system: "
                                + starSystem + ". When you get there scan nav beacon or search for resource sites. " +
                                "I may not be able to detect them automatically. Confirmaation is required."
                )
        );
        ReminderManager.getInstance().setDestination(
                new DataDto(starSystem, target.getTargetFaction()).toJson()
        );
    }

    record DataDto(String starSystem, String targetFaction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
