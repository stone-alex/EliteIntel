package elite.companion;

import elite.companion.comms.VoiceCommandInterpritor;
import elite.companion.comms.VoiceNotifier;
import elite.companion.subscribers.CarrierEventSubscriber;
import elite.companion.subscribers.CommanderEventSubscriber;
import elite.companion.subscribers.LoadGameEventSubscriber;
import elite.companion.subscribers.MiningEventSubscriber;


public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceNotifier.getInstance().speak("Initializing Companion", VoiceNotifier.CHARLES);

        new LoadGameEventSubscriber();
        new MiningEventSubscriber();
        new CarrierEventSubscriber();
        new CommanderEventSubscriber();
        new VoiceCommandInterpritor();

        VoiceNotifier.getInstance().speak("Initialization complete. Your mic is hot, the big brother is listening...", VoiceNotifier.JENNIFER);

        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}