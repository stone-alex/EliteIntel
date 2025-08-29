package elite.companion;

import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.JournalParser;
import elite.companion.session.SystemSession;
import elite.companion.util.Globals;
import elite.companion.util.SubscriberRegistration;

public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceGenerator.getInstance().speak("Initializing " + Globals.AI_NAME);

        SubscriberRegistration.registerSubscribers();

        AuxiliaryFilesMonitor monitor = new AuxiliaryFilesMonitor();
        new Thread(monitor).start();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread

        if (SystemSession.getInstance().getObject(SystemSession.RANK) == null) {
            VoiceGenerator.getInstance().speak("No Game session data loaded.");
        }

        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}