package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.brain.handlers.commands.custom.CustomCommands;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MiningRefinedEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        PlayerSession playerSession = PlayerSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();
        if (playerSession.get(CustomCommands.SET_MINING_TARGET.getParamKey()) == null) {
            playerSession.put(CustomCommands.SET_MINING_TARGET.getParamKey(), dto.getTypeLocalised().replace("\"", ""));
            EventBusManager.publish(new SensorDataEvent("Set mining target to: " + dto.getTypeLocalised()));
        }
        log.info("Mining event processed: {}", dto.toString());
    }
}
