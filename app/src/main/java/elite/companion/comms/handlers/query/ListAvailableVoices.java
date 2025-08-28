package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.comms.voice.Voices;

public class ListAvailableVoices implements QueryHandler {

    @Override public String handle(String action, JsonObject params, String originalUserInput) throws Exception {

        Voices[] voices = Voices.values();
        StringBuilder sb = new StringBuilder();
        sb.append("Available voices are: ");
        for(Voices voice : voices) {
            sb.append(voice.getName()).append(", ");
        }
        VoiceGenerator.getInstance().speak(sb.toString());

        return sb.toString();
    }
}
