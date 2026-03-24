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
 * The STTSanitizer class is a singleton utility for sanitizing
 * transcripts and correcting common speech-to-text (STT) transcription mistakes.
 * It processes and replaces misheard words from STT results using a correction
 * dictionary, which is either loaded from or created in the application directory.
 * <p>
 * This class also provides helper functions such as word capitalization.
 */
public class STTSanitizer {

    private static final STTSanitizer INSTANCE = new STTSanitizer();
    private final Logger log = LogManager.getLogger(STTSanitizer.class);
    private final Map<String, String> STT_CORRECTIONS = loadCorrections();
    private Set<String> dictionary = new HashSet<>();

    private STTSanitizer() {
        // Private constructor for singleton
    }

    public static STTSanitizer getInstance() {
        return INSTANCE;
    }

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


    // Ensure corrections file exists, create with defaults if not

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

    public String correctMistakes(String transcript) {
        String sanitized = transcript.toLowerCase();
        // Normalize symbols that Whisper may output as characters rather than words
        sanitized = sanitized.replaceAll(" \\+(\\d)", " plus $1").replaceAll(" -(\\d)", " minus $1").replace(" + ", " plus ").replace(" - ", " minus ").replace(",", " ").replace("!", "").replace("?", "").replace(".", "");
        for (Map.Entry<String, String> entry : STT_CORRECTIONS.entrySet()) {
            sanitized = sanitized.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        }

        if (sanitized.startsWith("the")) {
            sanitized = sanitized.replaceFirst("the", "");
        }
        if (sanitized.startsWith("to")) {
            sanitized = sanitized.replaceFirst("to", "");
        }
        if (sanitized.startsWith("for")) {
            sanitized = sanitized.replaceFirst("for", "");
        }
        if (sanitized.startsWith("and")) {
            sanitized = sanitized.replaceFirst("and", "");
        }
        if (sanitized.startsWith("it's")) {
            sanitized = sanitized.replaceFirst("it's", "");
        }
        if (sanitized.startsWith("this is")) {
            sanitized = sanitized.replaceFirst("this is", "");
        }
        if (sanitized.startsWith("that")) {
            sanitized = sanitized.replaceFirst("that", "");
        }
        if (sanitized.startsWith("my")) {
            sanitized = sanitized.replaceFirst("my", "");
        }
        if (sanitized.startsWith("you")) {
            sanitized = sanitized.replaceFirst("you", "");
        }

        Set<String> tempDictionary = STT_CORRECTIONS.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
        tempDictionary.addAll(getCommodities());
        tempDictionary.addAll(subSystems());
        tempDictionary.addAll(getMaterials());
        /// Do not use fuzzy. Use params in Whisper instead.
        sanitized = SttTermCorrector.getInstance().correct(sanitized, new TreeSet<>(tempDictionary));
        return sanitized.trim();
    }
}