package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.MiningRefinedEvent;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class MiningEventSubscriber {

    private static final Logger log = LogManager.getLogger(MiningEventSubscriber.class);

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String material = dto.getTypeLocalised().replace("\"", "").toLowerCase();
        if(!playerSession.getMiningTargets().contains(material)) {
            playerSession.addMiningTarget(material);
            EventBusManager.publish(new VocalisationRequestEvent(material + " is added to mining targets"));
        }
    }
}
