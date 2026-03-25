package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.GoogleVoices;
import elite.intel.ai.mouth.kokoro.KokoroVoices;
import elite.intel.ai.mouth.subscribers.events.AiVoxDemoEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;

public class ListAvailableVoicesHandler implements CommandHandler {

    private final SystemSession session = SystemSession.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (session.useLocalTTS()) {
            KokoroVoices[] voices = KokoroVoices.values();
            for (KokoroVoices voice : voices) {
                KokoroVoices kokoroVoice = session.getKokoroVoice();
                if (!voice.getDisplayName().equals(kokoroVoice.getDisplayName())) {
                    EventBusManager.publish(new AiVoxDemoEvent(voice.getDisplayName() + ", " + voice.getDescription(), voice.name()));
                }
            }
        } else {
            GoogleVoices[] voices = GoogleVoices.values();
            for (GoogleVoices voice : voices) {
                if (!voice.getName().equals(session.getGoogleVoice().getName())) {
                    EventBusManager.publish(new AiVoxDemoEvent(voice.getName() + " at your service commander. ", voice.name()));
                }
            }
        }
    }
}