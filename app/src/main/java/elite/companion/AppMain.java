package elite.companion;

import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.JournalParser;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.util.EventBusManager;
import elite.companion.util.Globals;
import elite.companion.util.SubscriberRegistration;

public class AppMain {

    public static void main(String[] args) throws Exception {
        SubscriberRegistration.registerSubscribers();

        AuxiliaryFilesMonitor monitor = new AuxiliaryFilesMonitor();
        monitor.start();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread

        VoiceGenerator.getInstance().start();// Start the voice generator thread

        EventBusManager.publish(new VoiceProcessEvent(Globals.AI_NAME + " is online..."));

        JournalParser parser = new JournalParser();
        parser.startReading(); //NOTE: blocking in command line mode
    }
}