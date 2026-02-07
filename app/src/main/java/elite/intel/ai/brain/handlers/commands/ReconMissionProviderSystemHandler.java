package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.PirateHuntingGroundsDao.HuntingGround;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.db.managers.HuntingGroundManager.PirateMissionTuple;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.List;
import java.util.Map;

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

        List<PirateMissionTuple<HuntingGround, List<MissionProvider>>> huntingGrounds = huntingGroundManager.findInProviderForTargetStarSystem(currentLocation.getStarName(), null);

        /// if huntingGrounds is empty (we are not in a target system) try looking for matching target faction.
        if (huntingGrounds.isEmpty()) {
            MissionManager missionManager = MissionManager.getInstance();
            Map<Long, MissionDto> missions = missionManager.getMissions(missionManager.getPirateMissionTypes());
            if (!missions.isEmpty()) {
                String targetFaction = missions.values().stream().findFirst().get().getMissionTargetFaction();
                huntingGrounds = huntingGroundManager.findInProviderForTargetStarSystem(
                        huntingGroundManager.findStarSystemForFactionName(targetFaction),
                        null
                );
            }
        }


        MissionProvider provider = null;
        String targetStarSystemName = "";
        for (PirateMissionTuple<HuntingGround, List<MissionProvider>> pair : huntingGrounds) {
            List<MissionProvider> providers = pair.getMissionProvider();

            int size = providers.size();
            if (size == 1) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("There is " + size + " mission provider for " + pair.getTarget().getStarSystem()));
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("There are " + size + " mission providers for " + pair.getTarget().getStarSystem()));
            }

            provider = providers.stream().filter(p -> p.getMissionProviderFaction() == null).findFirst().orElse(null);
            targetStarSystemName = pair.getTarget().getStarSystem();
            if (provider != null) break;
        }

        if (provider == null) {
            EventBusManager.publish(
                    new MissionCriticalAnnouncementEvent(
                            "No mission providers targeting " + targetStarSystemName + " found. You need to be in target system or have an active pirate massacre type mission."
                    )
            );
            return;
        }

        HuntingGround target = huntingGrounds.stream().filter(
                data -> data.getTarget().getTargetFaction() == null
        ).findFirst().map(PirateMissionTuple::getTarget).orElse(null);

        String starSystem = provider.getStarSystem();
        EventBusManager.publish(
                new MissionCriticalAnnouncementEvent(
                        "Plotting route to " + starSystem + " when you get there look for factions targeting " + targetStarSystemName + " at local ports."
                )
        );

        RoutePlotter plotter = new RoutePlotter(controller);
        plotter.plotRoute(starSystem);
        ReminderManager.getInstance().setDestination(
                new DataDto(
                        starSystem,
                        target == null
                                ? " Seek mission providers in local ports with pirate massacre missions against " + targetStarSystemName + " system."
                                : " Target Faction: " + target.getTargetFaction()).toYaml()
        );
    }

    record DataDto(String starSystem, String targetFaction) implements ToYamlConvertable {

        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}