package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetAnnounceBodyScansHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.ANNOUNCE_STELLAR_BODY_SCANS.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession.getInstance().updateSession(SystemSession.ANNOUNCE_BODY_SCANS, isOn);
        VoiceGenerator.getInstance().speak(responseText);
    }
}
