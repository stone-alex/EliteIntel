package elite.companion;

import elite.companion.subscribers.*;


public class AppMain {
    public static void main(String[] args) throws Exception {


        new LoadGameEventSubscriber();
        new MiningEventSubscriber();
        new CarrierEventSubscriber();
        new CommanderEventSubscriber();


        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}