package elite.intel.ai.ears.parakeet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encodes raw hotwords into space-separated BPE token sequences compatible
 * with sherpa-onnx's NeMo transducer hotword format.
 * <p>
 * Parakeet uses a BPE subword tokenizer. The hotwords file must contain
 * space-separated token pieces (e.g. "▁w ea p on s"), not raw words.
 * This class performs greedy longest-match BPE encoding using the model's
 * tokens.txt vocabulary.
 */
public class HotwordEncoder {

    private static final Logger log = LogManager.getLogger(HotwordEncoder.class);

    private final List<String> sortedTokens;
    private final String boundary; // word-start marker (e.g. ▁), read from tokens.txt itself

    public HotwordEncoder(Path tokensFile) throws IOException {
        List<String> lines = Files.readAllLines(tokensFile, StandardCharsets.UTF_8);
        Set<String> vocab = new HashSet<>();
        String foundBoundary = null;
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;
            String token = parts[0];
            if (token.startsWith("<")) continue; // skip <unk>, <blank>, etc.
            vocab.add(token);
            // Extract the word-boundary marker from the first multi-char token that starts
            // with a non-letter (e.g. "▁t" → boundary = "▁"). Read from file to avoid
            // any character encoding mismatch with hardcoded literals in source.
            if (foundBoundary == null && token.length() >= 2 && !Character.isLetter(token.charAt(0))) {
                foundBoundary = String.valueOf(token.charAt(0));
            }
        }
        this.boundary = foundBoundary != null ? foundBoundary : "\u2581"; // U+2581 fallback
        // Sort longest-first so greedy match always picks the longest token
        this.sortedTokens = new ArrayList<>(vocab);
        this.sortedTokens.sort((a, b) -> Integer.compare(b.length(), a.length()));
        log.debug("HotwordEncoder loaded {} BPE tokens, boundary marker U+{}", sortedTokens.size(),
                Integer.toHexString(this.boundary.charAt(0)).toUpperCase());
    }

    /**
     * BPE-encode a single word into a space-separated token sequence.
     * Returns empty string if the word cannot be encoded.
     */
    public String encode(String word) {
        // BPE word-boundary marker must prefix the first token of each word.
        // Use the character read from tokens.txt to avoid encoding mismatches.
        String text = boundary + word.toLowerCase().trim();
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            boolean found = false;
            for (String token : sortedTokens) {
                if (text.startsWith(token, i)) {
                    if (!result.isEmpty()) result.append(' ');
                    result.append(token);
                    i += token.length();
                    found = true;
                    break;
                }
            }
            if (!found) {
                // No token covers this character - skip it (rare with a full BPE vocab)
                log.trace("HotwordEncoder: no token for char '{}' in word '{}'", text.charAt(i), word);
                i++;
            }
        }
        return result.toString().trim();
    }

    /**
     * Encodes every hotword in rawFile and writes the result to a temp file.
     * Lines that are blank or purely numeric are skipped.
     * Returns the path to the temp file (deleted on JVM exit).
     */
    public Path encodeFile(Path rawFile) throws IOException {
        List<String> encoded = new ArrayList<>();
        int skipped = 0;
        for (String line : Files.readAllLines(rawFile, StandardCharsets.UTF_8)) {
            String word = line.trim();
            if (word.isEmpty() || word.matches("[0-9]+")) continue;
            String tokenized = encode(word);
            if (tokenized.isEmpty()) {
                log.warn("HotwordEncoder: could not encode '{}' - skipping", word);
                skipped++;
            } else {
                encoded.add(tokenized);
            }
        }
        Path tmp = Files.createTempFile("parakeet_hotwords_", ".txt");
        tmp.toFile().deleteOnExit();
        Files.write(tmp, encoded, StandardCharsets.UTF_8);
        log.info("HotwordEncoder: {} hotwords encoded, {} skipped → {}", encoded.size(), skipped, tmp);
        return tmp;
    }
}
