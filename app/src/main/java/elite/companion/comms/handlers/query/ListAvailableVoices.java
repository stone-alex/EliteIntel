package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.comms.mouth.Voices;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import java.util.ArrayList;
import java.util.List;

public class ListAvailableVoices implements QueryHandler {
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        SystemSession session = SystemSession.getInstance();
        PlayerSession playerSession = PlayerSession.getInstance();
        Voices[] voices = Voices.values();
        List<String> voiceNames = new ArrayList<>();
        for (Voices voice : voices) {
            if (!voice.getName().equals(session.getAIVoice().getName())) {
                voiceNames.add(voice.getName());
            }
        }
        return GenericResponse.getInstance().genericResponseWithList(
                "Available voices: " + String.join(", ", voiceNames) + ", " + playerSession.get(PlayerSession.PLAYER_NAME),
                voiceNames
        );
    }
}