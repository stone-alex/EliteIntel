package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

import java.util.ArrayList;
import java.util.List;

public class ListAvailableVoices implements QueryHandler {
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        SystemSession session = SystemSession.getInstance();
        PlayerSession playerSession = PlayerSession.getInstance();
        AiVoices[] voices = AiVoices.values();
        List<String> voiceNames = new ArrayList<>();
        for (AiVoices voice : voices) {
            if (!voice.getName().equals(session.getAIVoice().getName())) {
                voiceNames.add(voice.getName());
            }
        }
        return GenericResponse.getInstance().genericResponseWithList(
                "Available voices: " + String.join(", ", voiceNames) + ", " + playerSession.getPlayerName(),
                voiceNames
        );
    }
}