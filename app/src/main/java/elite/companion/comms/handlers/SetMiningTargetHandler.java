package elite.companion.comms.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.VoiceGenerator;
import elite.companion.session.PublicSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import elite.companion.comms.CommandAction;

public class SetMiningTargetHandler implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(SetMiningTargetHandler.class);

    @Override
    public void handle(JsonObject params, String responseText) {
        PublicSession session = PublicSession.getInstance();
        JsonElement jsonElement = params.get("mining_target");

        if (jsonElement == null || jsonElement.getAsString().isEmpty()) {
            log.info("no mining target set");
            VoiceGenerator.getInstance().speak("No material set. Please try again.");
        } else {
            session.updateSession(CommandAction.SET_MINING_TARGET.getParamKey(), jsonElement.getAsJsonPrimitive().getAsString().replace("\"", ""));
            VoiceGenerator.getInstance().speak(responseText);
        }
    }
}