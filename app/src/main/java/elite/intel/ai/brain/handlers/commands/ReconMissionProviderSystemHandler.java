package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.db.managers.PirateMissionDataManager.PirateMissionTuple;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.conscrypt.OpenSSLCipherRSA;

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
        for (PirateMissionTuple<PirateFaction, List<MissionProvider>> pair : huntingGrounds) {
            List<MissionProvider> providers = pair.getMissionProvider();
            provider = providers.stream().filter(p -> p.getMissionProviderFaction() == null).findFirst().orElse(null);
            if (provider != null) break;
        }

        if (provider == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No mission providers found."));
            return;
        }

        String starSystem = provider.getStarSystem();
        RoutePlotter plotter = new RoutePlotter(controller);
        plotter.plotRoute(starSystem);
        DestinationReminderManager.getInstance().setDestination(
                new DataDto(starSystem).toJson()
        );
    }

    record DataDto(String starSystem) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
