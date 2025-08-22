package elite.companion.comms;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.io.IOException;


public class STTTest {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        config.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        config.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        config.setGrammarPath("file:src/main/resources");

        try {
            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(config);
            recognizer.startRecognition(true);
            System.out.println("Say: We are mining Tritium");
            String result = recognizer.getResult().getHypothesis();
            System.out.println("Heard: " + result);
            recognizer.stopRecognition();
        } catch (IOException e) {
            System.err.println("Sphinx error: " + e.getMessage());
        }
    }
}
