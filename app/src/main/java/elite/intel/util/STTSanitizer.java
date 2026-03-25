package elite.intel.util;

import elite.intel.ai.ears.whisper.WhisperSTTImpl;
import elite.intel.db.dao.CommodityDao;
import elite.intel.db.dao.MaterialsDao;
import elite.intel.db.dao.SubSystemDao;
import elite.intel.db.util.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Singleton class used to sanitize and correct errors in Speech-To-Text (STT) transcripts.
 * The class provides mechanisms to load correction mappings, normalize text, and apply
 * replacements to STT outputs. It leverages predefined correction dictionaries and domain-specific
 * vocabularies to enhance the accuracy of transcriptions.
 */
public class STTSanitizer {

    private static final STTSanitizer INSTANCE = new STTSanitizer();
    private final Logger log = LogManager.getLogger(STTSanitizer.class);
    private final Map<String, String> STT_CORRECTIONS = loadCorrections();

    private STTSanitizer() {
        // Private constructor for singleton
    }

    public static STTSanitizer getInstance() {
        return INSTANCE;
    }

    /**
     * Determines the application directory used for storing the dictionary files.
     * The method attempts to identify if the application is running from a JAR file,
     * and if so, sets the directory path relative to the JAR's parent directory.
     * If an error occurs or the JAR's location cannot be determined, a default directory
     * path is used.
     *
     * @return A string representing the path to the application's directory, either
     * derived from the JAR's location or a default fallback value.
     */
    private String determineAppDir() {
        String appDir = "distribution" + File.separator + "dictionary/";
        try {
            URI jarUri = WhisperSTTImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator + "dictionary/";
                    log.debug("Running from JAR, set APP_DIR to: {}", appDir);
                } else {
                    log.warn("JAR parent directory is null, using default APP_DIR: {}", appDir);
                }
            }
        } catch (Exception e) {
            log.warn("Could not determine JAR location, using default APP_DIR: {}. Error: {}", appDir, e.getMessage());
        }
        return appDir;
    }


    private Set<String> subSystems() {
        return Database.withDao(SubSystemDao.class, dao -> new HashSet<>(dao.getAllNamesLowerCase()));
    }

    private Set<String> getMaterials() {
        return Database.withDao(MaterialsDao.class, dao -> new HashSet<>(dao.getAllNamesLowerCase()));
    }

    private Set<String> getCommodities() {
        return Database.withDao(CommodityDao.class, dao -> new HashSet<>(dao.getAllNamesLowerCase()));
    }


    /**
     * Ensures the existence of the STT corrections file in the application's directory.
     * If the corrections file does not exist, it creates a new file with a default template.
     * The file is used to correct common misinterpretations of the Speech-To-Text (STT) system.
     *
     * @return A {@code File} object representing the corrections file, or {@code null} if the file
     * could not be created due to errors (e.g., directory creation failure).
     */
    private File ensureCorrectionsFileExists() {
        String APP_DIR = determineAppDir();
        String CORRECTIONS_FILE = "stt-correction-dictionary.txt";
        File file = new File(APP_DIR, CORRECTIONS_FILE);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                log.error("Failed to create session directory: {}", parentDir.getPath());
                return null;
            }
        }
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("###########################################################################\n");
                writer.write("# STT is a deff and dumb secretary. We have to correct it's mistakes.\n");
                writer.write("# List of words or phrases to be corrected\n");
                writer.write("# Add your own, if the Speech To Text can't understand you well.\n");
                writer.write("# Surround the key and value with quotes\n");
                writer.write("# Speech to Text corrections (format: \"misheard\"=\"corrected\")\n");
                writer.write("# Example: \"treat you\"=tritium\n");

                log.info("Created default corrections file: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create default corrections file: {}. Error: {}", file.getPath(), e.getMessage());
            }
        }
        log.info("Corrections file: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * Loads Speech-To-Text (STT) correction mappings from a predefined corrections file.
     * The corrections file contains key-value pairs where the key represents the misheard
     * text and the value represents its corrected form. Lines starting with '#' or empty
     * lines are ignored during parsing. Keys and values are normalized to lower case and
     * stripped of any quotation marks.
     * <p>
     * If the corrections file does not exist, it will be created using a default template
     * by the {@code ensureCorrectionsFileExists()} method.
     *
     * @return A map containing the loaded corrections, where each key is a normalized
     * misheard string and its corresponding value is the corrected text.
     * Returns an empty map if the corrections file cannot be read or does not exist.
     */
    private Map<String, String> loadCorrections() {
        Map<String, String> map = new HashMap<>();
        File file = ensureCorrectionsFileExists();
        if (file != null && file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            map.put(parts[0].trim().toLowerCase().replaceAll("\"", ""), parts[1].trim().toLowerCase().replaceAll("\"", ""));
                        }
                    }
                }
                log.info("Loaded {} STT corrections from {}", map.size(), file.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to load STT corrections from {}: {}", file.getPath(), e.getMessage());
            }
        }

        return map;
    }

    /**
     * Corrects errors and normalizes text in a given transcript by applying several transformations
     * such as replacing specific patterns, applying substitution rules, and trimming redundant
     * prefixes from the beginning of the text. It also leverages domain-specific dictionaries
     * to adjust and refine the final output.
     *
     * @param transcript The original Speech-To-Text (STT) transcript that may contain errors or
     *                   inconsistencies to be corrected.
     * @return A sanitized and corrected version of the provided transcript, with adjustments based
     * on predefined correction mappings and domain-specific lexicons.
     */
    public String correctMistakes(String transcript) {
        String sanitized = transcript.toLowerCase();
        // Normalize symbols that Whisper may output as characters rather than words
        sanitized = sanitized.replaceAll(" \\+(\\d)", " plus $1").replaceAll(" -(\\d)", " minus $1").replace(" + ", " plus ").replace(" - ", " minus ").replace(",", " ").replace("!", "").replace("?", "").replace(".", "");
        for (Map.Entry<String, String> entry : STT_CORRECTIONS.entrySet()) {
            sanitized = sanitized.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        }

        Collection<String> startWordsToRemove = getStartWordsToRemove();
        for (String word : startWordsToRemove) {
            sanitized = sanitized.replaceFirst(word + " ", "");
        }

        Set<String> tempDictionary = STT_CORRECTIONS.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
        tempDictionary.addAll(getTerms());
        tempDictionary.addAll(getCommodities());
        tempDictionary.addAll(subSystems());
        tempDictionary.addAll(getMaterials());
        /// Do not use fuzzy. Use params in Whisper instead.
        sanitized = SttTermCorrector.getInstance().correct(sanitized, new TreeSet<>(tempDictionary));
        return sanitized.trim();
    }

    /**
     * Retrieves a collection of words or phrases that should be removed if they appear at the start of a text.
     * These words are typically filler or redundant expressions that do not add meaningful value to the context.
     *
     * @return A collection of strings representing the words or phrases to be removed from the start of a text.
     */
    private Collection<String> getStartWordsToRemove() {
        return Arrays.asList("and", "it's", "this is", "that", "my", "you", "for", "to", "the", "or", "i", "let me", "but", "so", "did", "i'm going");
    }

    /**
     * Retrieves a predefined collection of terms relevant to operations, such as handling UI components
     * or user interactions. These terms might be used for filtering, matching, or processing purposes
     * within the application.
     *
     * @return A collection of strings representing operation-related terms, such as "window", "close",
     * "panel", "exit", "open", "display", "hide", "dismiss", "dismissed", and "deploy".
     */
    private Collection<String> getTerms() {
        return Arrays.asList("email", "show", "equalize", "window", "recall", "close", "panel", "exit", "open", "display", "hide", "dismiss", "dismissed", "deploy", "drill", "mine", "mining");
    }
}