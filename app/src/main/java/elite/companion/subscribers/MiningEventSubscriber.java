package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import elite.companion.EventBusManager;
import elite.companion.comms.*;
import elite.companion.events.MiningRefinedEvent;
import elite.companion.session.SessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.Globals.EXTERNAL_TRANSMISSION;

public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);

    public MiningEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        VoiceGenerator.getInstance().speak("One ton of " + dto.getTypeLocalised() + " has been refined!");
        SessionTracker session = SessionTracker.getInstance();
        if (session.getObject(CommandAction.SET_MINING_TARGET.getParamKey()) == null) {
            session.updateSession(CommandAction.SET_MINING_TARGET.getParamKey(), dto.getTypeLocalised().replace("\"", ""));
            session.updateSession(EXTERNAL_TRANSMISSION, "Set mining target to: " + dto.getTypeLocalised());
        }
        log.info("Mining event processed: {}", dto.toString());
    }
}
