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


/*

GrokCommandProcessor processor = GrokCommandProcessor.getInstance();
        processor.start();

// Simulate Grok JSON responses
String landingGearResponse = "{\"type\": \"command\", \"action\": \"LandingGearToggle\", \"response_text\": \"Deploying landing gear\", \"params\": {}}";
String hyperspaceResponse = "{\"type\": \"command\", \"action\": \"Hyperspace\", \"response_text\": \"Initiating hyperspace jump\", \"params\": {}}";
String shieldCellResponse = "{\"type\": \"command\", \"action\": \"UseShieldCell\", \"response_text\": \"Activating shield cell\", \"params\": {}}";
String invalidResponse = "{\"type\": \"command\", \"action\": \"PrimaryFire\", \"response_text\": \"Firing weapon\", \"params\": {}}";
String queryResponse = "{\"type\": \"query\", \"response_text\": \"The galaxy map shows your current star system.\"}";

// Test responses
        processor.processResponse(landingGearResponse); // Executes Key_L, speaks "Deploying landing gear"
        Thread.sleep(1000);
        processor.processResponse(hyperspaceResponse); // Executes Key_J, speaks "Initiating hyperspace jump"
        Thread.sleep(1000);
        processor.processResponse(shieldCellResponse); // Executes Key_G, speaks "Activating shield cell"
        Thread.sleep(1000);
        processor.processResponse(invalidResponse); // Blocked, speaks "Sorry, that action is not allowed."
        Thread.sleep(1000);
        processor.processResponse(queryResponse); // Speaks "The galaxy map shows your current star system."

// Clean up
        processor.stop();*/
