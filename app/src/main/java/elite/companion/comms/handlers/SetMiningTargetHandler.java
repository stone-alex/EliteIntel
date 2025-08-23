package elite.companion.comms.handlers;

import elite.companion.comms.VoiceGenerator;
import elite.companion.session.SessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

public class SetMiningTargetHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(SetMiningTargetHandler.class);

    @Override
    public void handle(JsonObject params, String responseText) {
        String target = params.has("target") ? params.get("target").getAsString() : "";
        if (!target.isEmpty()) {
            SessionTracker.getInstance().updateSession("mining_target", target);
            log.info("Set mining target to: {}", target);
            VoiceGenerator.getInstance().speak(responseText);
        } else {
            log.warn("No mining target specified in params");
            VoiceGenerator.getInstance().speak("Please specify a mining target.");
        }
    }
}