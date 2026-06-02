package elite.intel.ai.hands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Safely applies the first editing MVP: assign one plain keyboard key to one
 * Primary or Secondary slot, or clear one selected slot, in an Elite Dangerous
 * {@code .binds} file.
 * <p>
 * This writer is intentionally separate from {@link KeyBindingsParser}. The
 * parser feeds command execution and must remain a read-only, keyboard-only
 * boundary; write support should not make non-keyboard assignments executable
 * by accident.
 */
public class BindingsWriter {
    private static final byte[] UTF_8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final KeyboardKeyAvailabilityService availabilityService;
    private final BindingsBackupService backupService;

    public BindingsWriter() {
        this(new KeyboardKeyAvailabilityService(), new BindingsBackupService());
    }

    BindingsWriter(KeyboardKeyAvailabilityService availabilityService, BindingsBackupService backupService) {
        this.availabilityService = availabilityService;
        this.backupService = backupService;
    }

    /**
     * Replaces exactly one selected slot with a plain keyboard slot or the
     * canonical Elite Dangerous empty slot.
     * <p>
     * The method rejects stale files before and after backup creation, validates
     * the key against the full active file, and edits raw XML text instead of
     * using a DOM Transformer. That avoids reformatting the whole file and keeps
     * unrelated XML byte-for-byte identical apart from the selected slot.
     */
    public BindingSaveResult assignKeyboardKey(KeyboardBindingEdit edit) {
        if (!edit.clearsSlot() && !EliteKeyboardKeys.isAssignable(edit.key())) {
            return BindingSaveResult.UNKNOWN_KEY;
        }

        try {
            if (isStale(edit)) {
                return BindingSaveResult.STALE_FILE;
            }

            EncodedXml encodedXml = readXml(edit.file());
            LocatedAction action = locateAction(encodedXml.xml(), edit.bindingId());
            if (action.result() != null) {
                return action.result();
            }

            LocatedSlot slot = locateSlot(encodedXml.xml(), action.range(), edit.slotType());
            if (slot.result() != null) {
                return slot.result();
            }

            if (isNoChange(encodedXml.xml(), slot.range(), edit)) {
                return BindingSaveResult.NO_CHANGE;
            }

            if (!isSupportedSlotForReplacement(encodedXml.xml(), slot.range(), edit.slotType())) {
                return BindingSaveResult.UNSUPPORTED_XML;
            }

            if (!edit.clearsSlot() && availabilityService.isKeyOccupiedByOtherSlot(
                    edit.file(),
                    edit.bindingId(),
                    edit.slotType(),
                    edit.key()
            )) {
                return BindingSaveResult.KEY_OCCUPIED;
            }

            if (!createBackup(edit.file())) {
                return BindingSaveResult.BACKUP_FAILED;
            }

            if (isStale(edit)) {
                return BindingSaveResult.STALE_FILE;
            }

            String updatedXml = replaceRange(encodedXml.xml(), slot.range(), replacementSlot(edit));
            return writeReplacement(edit.file(), new EncodedXml(updatedXml, encodedXml.hasUtf8Bom()));
        } catch (IOException e) {
            return BindingSaveResult.WRITE_FAILED;
        } catch (Exception e) {
            return BindingSaveResult.UNSUPPORTED_XML;
        }
    }

    private boolean createBackup(Path file) {
        try {
            backupService.createBackup(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private EncodedXml readXml(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        boolean hasBom = hasUtf8Bom(bytes);
        int offset = hasBom ? UTF_8_BOM.length : 0;
        return new EncodedXml(new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8), hasBom);
    }

    private boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= UTF_8_BOM.length
                && bytes[0] == UTF_8_BOM[0]
                && bytes[1] == UTF_8_BOM[1]
                && bytes[2] == UTF_8_BOM[2];
    }

    private BindingSaveResult writeReplacement(Path file, EncodedXml updatedXml) {
        Path tempFile = file.getParent().resolve(
                "." + file.getFileName() + ".elite-intel-" + UUID.randomUUID() + ".tmp");
        try {
            Files.write(
                    tempFile,
                    encode(updatedXml),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
            try {
                Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return BindingSaveResult.SAVED;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // Best-effort cleanup; the save result already reports failure.
            }
            return BindingSaveResult.WRITE_FAILED;
        }
    }

    private byte[] encode(EncodedXml encodedXml) {
        byte[] content = encodedXml.xml().getBytes(StandardCharsets.UTF_8);
        if (!encodedXml.hasUtf8Bom()) {
            return content;
        }

        byte[] withBom = new byte[UTF_8_BOM.length + content.length];
        System.arraycopy(UTF_8_BOM, 0, withBom, 0, UTF_8_BOM.length);
        System.arraycopy(content, 0, withBom, UTF_8_BOM.length, content.length);
        return withBom;
    }

    private boolean isStale(KeyboardBindingEdit edit) throws IOException {
        if (edit.expectedLastModified() == null || edit.expectedFileSize() < 0) {
            return true;
        }
        FileTime actualLastModified = Files.getLastModifiedTime(edit.file());
        long actualSize = Files.size(edit.file());
        return !actualLastModified.equals(edit.expectedLastModified())
                || actualSize != edit.expectedFileSize();
    }

    private boolean isNoChange(String xml, TextRange slotRange, KeyboardBindingEdit edit) {
        String startTag = startTag(xml, slotRange.start());
        String device = attributeValue(startTag, "Device");
        String currentKey = attributeValue(startTag, "Key");
        if (edit.clearsSlot()) {
            return "{NoDevice}".equals(device) && currentKey.isBlank();
        }
        return "Keyboard".equals(device) && edit.key().equals(currentKey);
    }

    private boolean isSupportedSlotForReplacement(String xml, TextRange slotRange, BindingSlotType slotType) {
        String startTag = startTag(xml, slotRange.start());
        Set<String> allowedAttributes = Set.of("Device", "Key", "Hold");
        if (!allowedAttributes.containsAll(attributeNames(startTag))) {
            return false;
        }

        if (isSelfClosingStartTag(xml, slotRange.start(), findStartTagEnd(xml, slotRange.start()))) {
            return true;
        }

        int startTagEnd = findStartTagEnd(xml, slotRange.start());
        Matcher closingMatcher = closingTagPattern(slotType.xmlElementName()).matcher(xml);
        if (!closingMatcher.find(startTagEnd + 1) || closingMatcher.end() != slotRange.end()) {
            return false;
        }

        String body = xml.substring(startTagEnd + 1, closingMatcher.start());
        Matcher childMatcher = Pattern.compile("<(?!/|!|\\?)([A-Za-z_][A-Za-z0-9_.:-]*)(?=[\\s>/])").matcher(body);
        while (childMatcher.find()) {
            if (!"Modifier".equals(childMatcher.group(1))) {
                return false;
            }
        }
        return true;
    }

    private String replacementSlot(KeyboardBindingEdit edit) {
        // The MVP deliberately writes a plain single-key slot. Modifiers and
        // Hold are not preserved because this operation is not a combo editor.
        if (edit.clearsSlot()) {
            return "<" + edit.slotType().xmlElementName() + " Device=\"{NoDevice}\" Key=\"\" />";
        }
        return "<" + edit.slotType().xmlElementName()
                + " Device=\"Keyboard\" Key=\"" + edit.key() + "\" />";
    }

    private String replaceRange(String xml, TextRange range, String replacement) {
        return xml.substring(0, range.start()) + replacement + xml.substring(range.end());
    }

    private LocatedAction locateAction(String xml, String bindingId) {
        List<Integer> starts = openingTagStarts(xml, bindingId, 0, xml.length());
        if (starts.isEmpty()) {
            return new LocatedAction(BindingSaveResult.BINDING_NOT_FOUND, null);
        }
        if (starts.size() > 1) {
            return new LocatedAction(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        int start = starts.get(0);
        int startTagEnd = findStartTagEnd(xml, start);
        if (startTagEnd < 0 || isSelfClosingStartTag(xml, start, startTagEnd)) {
            return new LocatedAction(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        Matcher closingMatcher = closingTagPattern(bindingId).matcher(xml);
        if (!closingMatcher.find(startTagEnd + 1)) {
            return new LocatedAction(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        int closeStart = closingMatcher.start();
        if (!openingTagStarts(xml, bindingId, startTagEnd + 1, closeStart).isEmpty()) {
            // Nested or overlapping action tags make textual replacement unsafe;
            // fail closed rather than guessing which close tag belongs to us.
            return new LocatedAction(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        return new LocatedAction(null, new TextRange(startTagEnd + 1, closeStart));
    }

    private LocatedSlot locateSlot(String xml, TextRange actionBodyRange, BindingSlotType slotType) {
        List<Integer> starts = openingTagStarts(
                xml,
                slotType.xmlElementName(),
                actionBodyRange.start(),
                actionBodyRange.end()
        );
        if (starts.size() != 1) {
            // Missing and duplicate slots are both unsupported for the same
            // reason: this backend edits exactly one explicit slot.
            return new LocatedSlot(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        int start = starts.get(0);
        int startTagEnd = findStartTagEnd(xml, start);
        if (startTagEnd < 0 || startTagEnd >= actionBodyRange.end()) {
            return new LocatedSlot(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        if (isSelfClosingStartTag(xml, start, startTagEnd)) {
            return new LocatedSlot(null, new TextRange(start, startTagEnd + 1));
        }

        Matcher closingMatcher = closingTagPattern(slotType.xmlElementName()).matcher(xml);
        if (!closingMatcher.find(startTagEnd + 1) || closingMatcher.start() > actionBodyRange.end()) {
            return new LocatedSlot(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        int closeStart = closingMatcher.start();
        if (!openingTagStarts(xml, slotType.xmlElementName(), startTagEnd + 1, closeStart).isEmpty()) {
            return new LocatedSlot(BindingSaveResult.UNSUPPORTED_XML, null);
        }

        return new LocatedSlot(null, new TextRange(start, closingMatcher.end()));
    }

    private List<Integer> openingTagStarts(String xml, String tagName, int from, int to) {
        Pattern pattern = Pattern.compile("<" + Pattern.quote(tagName) + "(?=[\\s>/])");
        Matcher matcher = pattern.matcher(xml);
        List<Integer> starts = new ArrayList<>();
        int searchFrom = Math.max(0, from);
        while (matcher.find(searchFrom) && matcher.start() < to) {
            starts.add(matcher.start());
            searchFrom = matcher.end();
        }
        return starts;
    }

    private Pattern closingTagPattern(String tagName) {
        return Pattern.compile("</" + Pattern.quote(tagName) + "\\s*>");
    }

    private int findStartTagEnd(String xml, int tagStart) {
        char quote = 0;
        for (int i = tagStart; i < xml.length(); i++) {
            char c = xml.charAt(i);
            if ((c == '"' || c == '\'') && quote == 0) {
                quote = c;
            } else if (c == quote) {
                quote = 0;
            } else if (c == '>' && quote == 0) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSelfClosingStartTag(String xml, int tagStart, int tagEnd) {
        for (int i = tagEnd - 1; i > tagStart; i--) {
            char c = xml.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            return c == '/';
        }
        return false;
    }

    private String startTag(String xml, int tagStart) {
        int tagEnd = findStartTagEnd(xml, tagStart);
        if (tagEnd < 0) {
            return "";
        }
        return xml.substring(tagStart, tagEnd + 1);
    }

    private String attributeValue(String startTag, String attributeName) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(attributeName) + "\\s*=\\s*([\"'])(.*?)\\1");
        Matcher matcher = pattern.matcher(startTag);
        return matcher.find() ? matcher.group(2) : "";
    }

    private Set<String> attributeNames(String startTag) {
        Matcher matcher = Pattern.compile("\\s+([A-Za-z_:][A-Za-z0-9_.:-]*)\\s*=").matcher(startTag);
        Set<String> names = new HashSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private record TextRange(int start, int end) {
    }

    private record LocatedAction(BindingSaveResult result, TextRange range) {
    }

    private record LocatedSlot(BindingSaveResult result, TextRange range) {
    }

    private record EncodedXml(String xml, boolean hasUtf8Bom) {
    }
}
