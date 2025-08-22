package elite.companion;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.comms.VoiceCommandModule;
import elite.companion.comms.VoiceNotifier;
import elite.companion.modules.MiningModule;

import java.io.IOException;
import java.nio.file.*;


public class AppMain {
    public static void main(String[] args) throws Exception {
        VoiceNotifier voice = new VoiceNotifier();
        EventBus bus = new EventBus();
        VoiceCommandModule voiceCommandModule = new VoiceCommandModule(bus);


        //JournalParser parser = new JournalParser(bus);
        //parser.startReading();

        //voice.speak("Companion is running");
    }
}