package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommands;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MiningRefinedEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

@SuppressWarnings("unused")
public class MiningEventSubscriber {

    private static final Logger log = LogManager.getLogger(MiningEventSubscriber.class);

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        // need ideas what to do with this event.
/*
        PlayerSession playerSession = PlayerSession.getInstance();
        if (playerSession.get(CustomCommands.SET_MINING_TARGET.getParamKey()) == null) {
            playerSession.put(CustomCommands.SET_MINING_TARGET.getParamKey(), dto.getTypeLocalised().replace("\"", ""));
            EventBusManager.publish(new SensorDataEvent("Set mining target to: " + dto.getTypeLocalised()));
        }
        playerSession.saveSession();

        log.info("Mining event processed: {}", dto.toString());
*/
    }
}
