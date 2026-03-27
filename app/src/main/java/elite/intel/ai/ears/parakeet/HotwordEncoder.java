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
 * Sherpa-onnx strips the word-boundary prefix (▁) from every token before
 * doing the vocabulary ID lookup. This means a boundary token like {@code ▁app}
 * is looked up as {@code app} - which only succeeds if {@code app} is ALSO a
 * plain mid-word token in tokens.txt. Tokens like {@code ▁app}, {@code ▁wor},
 * {@code ▁bas}, {@code ▁trans} fail because their content is NOT a mid-word token.
 * <p>
 * Fix: for word-start position, only use boundary tokens whose content (after
 * stripping ▁) is also present as a mid-word token. This guarantees the
 * stripped lookup succeeds. If no such token matches the current position,
 * the algorithm falls through to single-character boundary tokens (▁a, ▁b, …)
 * which all have their letter content in the mid-word vocab.
 */
public class HotwordEncoder {

    private static final Logger log = LogManager.getLogger(HotwordEncoder.class);

    // Boundary tokens valid for word-start: those whose content (after stripping ▁)
    // is ALSO a plain mid-word token. Sorted by content length descending (greedy).
    private final List<String> validBoundaryTokens;
    // Plain tokens (mid-word, no boundary prefix), sorted by length desc.
    private final List<String> midTokens;

    public HotwordEncoder(Path tokensFile) throws IOException {
        List<String> lines = Files.readAllLines(tokensFile, StandardCharsets.UTF_8);
        List<String> bTokens = new ArrayList<>();
        List<String> mTokens = new ArrayList<>();
        Set<String> midSet = new HashSet<>();

        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;
            String token = parts[0];
            if (token.startsWith("<") || token.isEmpty()) continue;
            boolean isBoundary = !Character.isLetterOrDigit(token.charAt(0)) && token.length() >= 2;
            if (isBoundary) {
                bTokens.add(token);
            } else if (Character.isLetterOrDigit(token.charAt(0))) {
                mTokens.add(token);
                midSet.add(token);
            }
        }

        // Filter boundary tokens: only keep those whose content (token[1:]) is also
        // a mid-word token. Sherpa-onnx strips the boundary char before lookup, so
        // only these will resolve to a valid ID.
        List<String> validB = new ArrayList<>();
        for (String bToken : bTokens) {
            String content = bToken.substring(1);
            if (midSet.contains(content)) {
                validB.add(bToken);
            }
        }

        // Sort by content length descending so greedy match picks the longest first.
        validB.sort((a, b) -> Integer.compare(b.length() - 1, a.length() - 1));
        mTokens.sort((a, b) -> Integer.compare(b.length(), a.length()));

        this.validBoundaryTokens = validB;
        this.midTokens = mTokens;
        log.debug("HotwordEncoder: {} valid boundary (content∈mid) + {} mid-word tokens",
                validBoundaryTokens.size(), midTokens.size());
    }

    /**
     * BPE-encode a single word into a space-separated token sequence.
     * Word-start uses only boundary tokens whose stripped content is a mid-word token.
     */
    public String encode(String word) {
        String text = word.toLowerCase().trim();
        StringBuilder result = new StringBuilder();
        int i = 0;
        boolean wordStart = true;

        while (i < text.length()) {
            boolean found = false;
            if (wordStart) {
                for (String token : validBoundaryTokens) {
                    String content = token.substring(1);
                    if (!content.isEmpty() && text.startsWith(content, i)) {
                        if (!result.isEmpty()) result.append(' ');
                        result.append(token);
                        i += content.length();
                        wordStart = false;
                        found = true;
                        break;
                    }
                }
            } else {
                for (String token : midTokens) {
                    if (text.startsWith(token, i)) {
                        if (!result.isEmpty()) result.append(' ');
                        result.append(token);
                        i += token.length();
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                log.trace("HotwordEncoder: no token for char '{}' in word '{}'", text.charAt(i), word);
                i++;
                wordStart = false;
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
