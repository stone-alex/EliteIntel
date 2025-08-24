package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.*;
import elite.companion.events.MiningRefinedEvent;
import elite.companion.session.PublicSession;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.Globals.SENSOR_READING;

public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);

    public MiningEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        VoiceGenerator.getInstance().speak("One ton of " + dto.getTypeLocalised() + " has been refined!");
        PublicSession session = PublicSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();
        if (session.getObject(CommandAction.SET_MINING_TARGET.getParamKey()) == null) {
            session.updateSession(CommandAction.SET_MINING_TARGET.getParamKey(), dto.getTypeLocalised().replace("\"", ""));
            systemSession.updateSession(SENSOR_READING, "Detected "+dto.getTypeLocalised()+" refined. Therefore set mining target to: " + dto.getTypeLocalised());
        }
        log.info("Mining event processed: {}", dto.toString());
    }
}
