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
        new CommanderEventSubscriber();
        new StatisticsSubscriber();
        new ReceiveTextSubscriber();
        new ProspectorSubscriber();
        new FSSSignalDiscoveredSubscriber();
        new PowerPlaySubscriber();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread


        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}

/*
* Example
* SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start();

        GrokInteractionHandler handler = recognizer.getGrok();
        handler.processVoiceCommand("set tritium as mining target"); // Sets mining_target in session
        Thread.sleep(1000);
        handler.processVoiceCommand("where is the nearest material trader?"); // Queries INARA
        Thread.sleep(1000);
        handler.processVoiceCommand("yes"); // Plots route
        Thread.sleep(1000);
        handler.processVoiceCommand("deploy landing gear"); // Triggers LandingGearToggle
        Thread.sleep(1000);
        handler.processVoiceCommand("open cargo scoop"); // Triggers ToggleCargoScoop

        recognizer.shutdown();
*
*
* */