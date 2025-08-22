package elite.companion;

import elite.companion.comms.VoiceCommandInterpritor;
import elite.companion.comms.VoiceNotifier;
import elite.companion.subscribers.*;


public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceNotifier.getInstance().speak("Initializing Companion");

        new LoadGameEventSubscriber();
        new MiningEventSubscriber();
        new CarrierEventSubscriber();
        new CommanderEventSubscriber();
        new VoiceCommandInterpritor();
        new StatisticsSubscriber();
        new ReceiveTextSubscriber();

        VoiceNotifier.getInstance().speak("Initialization complete. Your mic is hot, the big brother is listening...");
        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}