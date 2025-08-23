package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import elite.companion.EventBusManager;
import elite.companion.comms.GrokInteractionHandler;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.MiningRefinedEvent;
import elite.companion.session.SessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);

    public MiningEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {

        VoiceGenerator.getInstance().speak("One ton of " +dto.getTypeLocalised() + " has been refined!");

        JsonObject params = (JsonObject) SessionTracker.getInstance().getObject("params");
        if (params == null || !params.has("material")) {
            GrokInteractionHandler grok = new GrokInteractionHandler();
            grok.processVoiceCommand("set mining target " + dto.getTypeLocalised());
        }
        log.info("Mining event processed: {}", dto.toString());
    }
}
