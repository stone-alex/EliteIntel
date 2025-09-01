package elite.companion.ui;

import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.JournalParser;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.ui.controller.MainController;
import elite.companion.util.EventBusManager;
import elite.companion.util.Globals;
import elite.companion.util.SubscriberRegistration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        String fxmlPath = "/elite/companion/ui/view/MainView.fxml";
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("Cannot find FXML file at: " + fxmlPath);
            System.err.println("Classpath: " + System.getProperty("java.class.path"));
            throw new IllegalStateException("Cannot find MainView.fxml");
        }
        System.out.println("Loading FXML from: " + fxmlUrl);
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 800, 300);
        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Elite Dangerous Companion");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop(){
        if(monitorThread !=null && monitorThread.isAlive()){
            monitorThread.interrupt();
            monitorThread.stop();
        }
        if(journalThread !=null && journalThread.isAlive()){
            journalThread.interrupt();
            journalThread.stop();
        }
        System.exit(0);
    }

    private static Thread monitorThread;
    private static Thread journalThread;

    public static void main(String[] args) {
        SubscriberRegistration.registerSubscribers();

        AuxiliaryFilesMonitor monitor = new AuxiliaryFilesMonitor();
        monitorThread = new Thread(monitor);
        monitorThread.start();

        SpeechRecognizer recognizer = new SpeechRecognizer();
        recognizer.start(); // Start STT voice command processing thread

        //noinspection ResultOfMethodCallIgnored
        VoiceGenerator.getInstance();// Start the voice generator thread

        EventBusManager.publish(new VoiceProcessEvent(Globals.AI_NAME + " is online..."));

        JournalParser parser = new JournalParser();
        journalThread = new Thread(parser);
        journalThread.start();

        launch(args);
    }
}