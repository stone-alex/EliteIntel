package elite.companion;

import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.JournalParser;
import elite.companion.gameapi.gamestate.subscribers.CargoChangedEventSubscriber;
import elite.companion.gameapi.gamestate.subscribers.RoutePlottedSubscriber;
import elite.companion.gameapi.gamestate.subscribers.StatusChangeSubscriber;
import elite.companion.session.SystemSession;
import elite.companion.gameapi.journal.subscribers.*;

public class AppMain {

    public static void main(String[] args) throws Exception {
        VoiceGenerator.getInstance().speak("Initializing " + Globals.AI_NAME);

        //Journal subscribers
        new CarrierJumpRequestSubscriber();
        new CommanderEventSubscriber();
        new FSDJumpSubscriber();
        new FSDTargetSubscriber();
        new FSSSignalDiscoveredSubscriber();
        new GameStartedEventSubscriber();
        new MiningEventSubscriber();
        new PowerPlaySubscriber();
        new ProspectorSubscriber();
        new TransmissionReceivedSubscriber();
        new StatisticsSubscriber();
        new SupercruiseExitedSubscriber();
        new TouchdownEventSubscriber();
        new LiftoffEventSubscriber();
        new BodyScanEventSubscriber();
        new ShipTargetedEventSubscriber();
        new LoadoutSubscriber();
        new SwitchSuitLoadoutSubscriber();
        new RankEventSubscriber();
        new BountyEventSubscriber();
        new ScannedEventSubscriber();
        new RoutClearedSubscriber();
        new MissionAcceptedHandler();

        //Game API subscribers
        new RoutePlottedSubscriber();
        new CargoChangedEventSubscriber();
        new StatusChangeSubscriber();




        AuxiliaryFilesMonitor monitor = new AuxiliaryFilesMonitor();
        new Thread(monitor).start();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread

        if(SystemSession.getInstance().getObject(SystemSession.RANK) == null) {
            VoiceGenerator.getInstance().speak("No Game session data loaded.");
        }

        JournalParser parser = new JournalParser();
        parser.startReading();
    }
}