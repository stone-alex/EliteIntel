package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.GoogleVoices;
import elite.intel.ai.mouth.kokoro.KokoroVoices;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;

import java.util.ArrayList;
import java.util.List;

public class ListAvailableVoices implements CommandHandler {

    private final SystemSession session = SystemSession.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (session.useLocalTTS()) {
            KokoroVoices[] voices = KokoroVoices.values();
            List<String> voiceNames = new ArrayList<>();
            for (KokoroVoices voice : voices) {
                if (!voice.getDisplayName().equals(session.getGoogleVoice().getName())) {
                    voiceNames.add(voice.getDisplayName());
                }
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Available voices:\n" + String.join(", ", voiceNames)));
        } else {
            GoogleVoices[] voices = GoogleVoices.values();
            List<String> voiceNames = new ArrayList<>();
            for (GoogleVoices voice : voices) {
                if (!voice.getName().equals(session.getGoogleVoice().getName())) {
                    voiceNames.add(voice.getName());
                }
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Available voices:\n" + String.join(", ", voiceNames)));
        }
    }
}