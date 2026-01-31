package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

import java.util.ArrayList;
import java.util.List;

public class  ListAvailableVoices implements CommandHandler {

    private final SystemSession session = SystemSession.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        AiVoices[] voices = AiVoices.values();
        List<String> voiceNames = new ArrayList<>();
        for (AiVoices voice : voices) {
            if (!voice.getName().equals(session.getAIVoice().getName())) {
                voiceNames.add(voice.getName());
            }
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Available voices:\n" + String.join(", ", voiceNames)));
    }
}