package elite.companion.comms.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.util.JsonParameterExtractor.extractParameter;

public class SetPrivacyMode implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetRouteHandler.class);

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PRIVACY_MODE.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.put(SystemSession.PRIVACY_MODE, isOn);
        if(isOn){
            VoiceGenerator.getInstance().speak("Privacy mode is on. Please prefix your commands to me with Computer or "+systemSession.getAIVoice().getName());
        } else {
            VoiceGenerator.getInstance().speak("Privacy mode is off... I am listening.");
        }
    }
}
