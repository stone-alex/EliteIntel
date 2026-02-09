package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.RedeemVoucherEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.Map;

public class RedeemVoucherSubscriber {

    private final MissionManager missionManager = MissionManager.getInstance();

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        String instructions = """
                    A bounty payment was awarded.
                    Notify user about credits received and what factions we received it from.
                """;
        EventBusManager.publish(
                new SensorDataEvent(
                        new DataDto("Bounty Payment Awarded", event).toYaml(),
                        instructions)
        );

        Map<Long, MissionDto> missions = missionManager.getMissions(
                missionManager.getPirateMissionTypes()
        );
        if (missions == null || missions.isEmpty()) {
            PlayerSession.getInstance().clearBounties();
        }
    }

    record DataDto(String info, RedeemVoucherEvent event) implements ToYamlConvertable {

        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
