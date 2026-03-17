package elite.intel.util;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.db.FuzzySearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Applies word-level fuzzy correction to STT output, catching close misspellings
 * of known colloquial terms from the command and query vocabulary.
 * <p>
 * Example: "drop blending gear" → "drop landing gear"
 * ("blending" is not in the vocabulary, "landing" is, edit distance = 2)
 * <p>
 * The vocabulary is sourced from {@link AiCommandsAndQueries#getVocabulary()}, so
 * new colloquial terms added there are automatically picked up here.
 * Runs as a second pass inside {@link STTSanitizer#correctMistakes(String)},
 * after dictionary replacements.
 */
public class SttTermCorrector {

    private static final SttTermCorrector INSTANCE = new SttTermCorrector();
    private final Logger log = LogManager.getLogger(SttTermCorrector.class);

    // Ignore tokens shorter than this — too risky for false-positives on common words
    private static final int MIN_TOKEN_LENGTH = 5;
    // Accept a correction only when edit distance is within this threshold
    private static final int MAX_EDIT_DISTANCE = 2;

    // Cached vocabulary from the command/query maps
    private final Set<String> vocabulary;

    private SttTermCorrector() {
        vocabulary = AiCommandsAndQueries.getInstance().getVocabulary();
        log.info("SttTermCorrector loaded {} colloquial terms from command/query vocabulary", vocabulary.size());
    }

    public static SttTermCorrector getInstance() {
        return INSTANCE;
    }

    /**
     * Corrects each token in the transcript that closely matches a known colloquial
     * term within {@value MAX_EDIT_DISTANCE} edits. Tokens that are already in the
     * vocabulary, shorter than {@value MIN_TOKEN_LENGTH} chars, or have no close
     * match are left unchanged.
     */
    public String correct(String transcript) {
        if (vocabulary.isEmpty()) return transcript;
        String[] tokens = transcript.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) result.append(' ');
            String lower = tokens[i].toLowerCase();
            if (lower.length() >= MIN_TOKEN_LENGTH && !vocabulary.contains(lower)) {
                String corrected = findClosestTerm(lower);
                result.append(corrected != null ? corrected : lower);
            } else {
                result.append(lower);
            }
        }
        return result.toString();
    }

    private String findClosestTerm(String token) {
        String best = null;
        int bestDist = MAX_EDIT_DISTANCE + 1;
        for (String candidate : vocabulary) {
            int dist = FuzzySearch.levenshteinDistance(token, candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return bestDist <= MAX_EDIT_DISTANCE ? best : null;
    }
}