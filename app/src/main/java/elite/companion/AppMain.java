package elite.companion;

import elite.companion.comms.SpeechRecognizer;
import elite.companion.comms.VoiceGenerator;
import elite.companion.subscribers.*;

public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceGenerator.getInstance().speak("Initializing Companion");

        //instantiate subscribers
        new LoadGameEventSubscriber();
        new MiningEventSubscriber();
        new CarrierEventSubscriber();
        new CommanderEventSubscriber();
        new StatisticsSubscriber();
        new ReceiveTextSubscriber();
        new ProspectorSubscriber();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread
        VoiceGenerator.getInstance().speak("Mic is hot, standing-by");

        JournalParser parser = new JournalParser();
        VoiceGenerator.getInstance().speak("Monitoring Journal");
        parser.startReading();
    }
}