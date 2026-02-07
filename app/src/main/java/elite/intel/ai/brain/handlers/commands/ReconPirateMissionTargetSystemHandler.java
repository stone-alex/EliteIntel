package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateHuntingGroundsDao.HuntingGround;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.HuntingGroundManager.PirateMissionTuple;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.ReminderManager;
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
        HuntingGroundManager manager = HuntingGroundManager.getInstance();
        LocationManager locationManager = LocationManager.getInstance();
        List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> huntingGrounds = manager.findTargetSystemInRangeForRecon(locationManager.getGalacticCoordinates());


        HuntingGround target = huntingGrounds.stream().filter(
                data -> data.getTarget().getTargetFaction() == null && !data.getTarget().isHasResSite()
        ).findFirst().map(PirateMissionTuple::getTarget).orElse(null);

        if (target == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No recognisance target systems found."));
            return;
        }

        boolean multipleMissionProviders = huntingGrounds.getFirst().getMissionProvider().size() > 1;
        if (multipleMissionProviders) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Routing to target star system with multiple mission providers."));
        }

        String starSystem = target.getStarSystem();

        EventBusManager.publish(
                new MissionCriticalAnnouncementEvent(
                        """
                                        Plotting route to target system: %s.
                                        When you get there scan nav beacon or search for resource sites.
                                        I may not be able to detect them automatically.
                                        Confirmation is required.
                                        If scans do not trigger confirmation, you may need to manually confirm the presence of resource sites.
                                """.formatted(starSystem)
                )
        );
        ReminderManager.getInstance().setDestination(
                new DataDto(starSystem, target.getTargetFaction()).toJson()
        );

        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(starSystem);
    }

    record DataDto(String starSystem, String targetFaction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
