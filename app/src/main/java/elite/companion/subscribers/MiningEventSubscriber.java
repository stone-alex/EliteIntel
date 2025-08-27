package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.handlers.command.CommandAction;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.events.MiningRefinedEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);

    public MiningEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        VoiceGenerator.getInstance().speak("One ton of " + dto.getTypeLocalised() + " has been refined!");
        PlayerSession playerSession = PlayerSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();
        if (playerSession.getObject(CommandAction.SET_MINING_TARGET.getParamKey()) == null) {
            playerSession.updateSession(CommandAction.SET_MINING_TARGET.getParamKey(), dto.getTypeLocalised().replace("\"", ""));
            systemSession.setSensorData("Detected "+dto.getTypeLocalised()+" refined. Set mining target to: " + dto.getTypeLocalised());
        }
        log.info("Mining event processed: {}", dto.toString());
    }
}
