package elite.intel.util;

import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.db.FuzzySearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
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
 * Vocabulary is loaded lazily on first use so that all singletons are guaranteed
 * to be fully initialised before the word list is built.
 * Runs as a second pass inside {@link STTSanitizer#correctMistakes(String)},
 * after dictionary replacements.
 */
public class SttTermCorrector {

    private static final SttTermCorrector INSTANCE = new SttTermCorrector();
    private final Logger log = LogManager.getLogger(SttTermCorrector.class);

    // Ignore tokens shorter than this - too risky for false-positives on common words
    private static final int MIN_TOKEN_LENGTH = 4;
    // Accept a correction only when edit distance is within this threshold
    private static final int MAX_EDIT_DISTANCE = 2;

    // Lazily populated on first call to correct()
    private volatile Set<String> vocabulary = null;

    private SttTermCorrector() {
    }

    public static SttTermCorrector getInstance() {
        return INSTANCE;
    }

    private Set<String> getVocabulary() {
        if (vocabulary == null) {
            synchronized (this) {
                if (vocabulary == null) {
                    try {
                        Set<String> v = AiCommandsAndQueries.getInstance().getVocabulary();
                        log.info("SttTermCorrector loaded {} colloquial terms from command/query vocabulary", v.size());
                        vocabulary = v;
                    } catch (Exception e) {
                        log.warn("SttTermCorrector could not build vocabulary: {}", e.getMessage());
                        vocabulary = Collections.emptySet();
                    }
                }
            }
        }
        return vocabulary;
    }

    /**
     * Corrects each token in the transcript that closely matches a known colloquial
     * term within {@value MAX_EDIT_DISTANCE} edits. Tokens that are already in the
     * vocabulary, shorter than {@value MIN_TOKEN_LENGTH} chars, or have no close
     * match are left unchanged.
     */
    public String correct(String transcript) {
        Set<String> vocab = getVocabulary();
        if (vocab.isEmpty()) return transcript;

        String[] tokens = transcript.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) result.append(' ');
            String raw = tokens[i].toLowerCase();
            String lower = raw.replaceAll("[^a-z]", ""); // strip punctuation before lookup
            if (lower.length() >= MIN_TOKEN_LENGTH && !vocab.contains(lower)) {
                String corrected = findClosestTerm(lower, vocab);
                result.append(corrected != null ? corrected : raw);
            } else {
                result.append(raw);
            }
        }
        return result.toString();
    }

    private static String findClosestTerm(String token, Set<String> vocab) {
        String best = null;
        int bestDist = MAX_EDIT_DISTANCE + 1;
        for (String candidate : vocab) {
            // Skip candidates that are just the token with a suffix added or removed
            // (e.g. "trade" vs "trader") - these are related forms, not corrections.
            if (candidate.startsWith(token) || token.startsWith(candidate)) continue;

            int dist = FuzzySearch.levenshteinDistance(token, candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return bestDist <= MAX_EDIT_DISTANCE ? best : null;
    }
}