package elite.companion;

import elite.companion.comms.SpeechRecognizer;
import elite.companion.comms.VoiceGenerator;
import elite.companion.subscribers.*;

public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceGenerator.getInstance().speak("Initializing Companion");

        new LoadGameEventSubscriber();
        new MiningEventSubscriber();
        new CarrierEventSubscriber();
        new CommanderEventSubscriber();
        new StatisticsSubscriber();
        new ReceiveTextSubscriber();
        new ProspectorSubscriber();

        VoiceGenerator.getInstance().speak("Mic is hot, standing-by");
        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start streaming in background
/*
        recognizer.startWavRecording();
        Thread.sleep(10000);
        recognizer.stopWavRecording();
*/

        VoiceGenerator.getInstance().speak("Monitoring Journal");
        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}