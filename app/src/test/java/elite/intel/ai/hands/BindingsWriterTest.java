package elite.intel.ai.hands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BindingsWriterTest {
    @TempDir
    Path tempDir;

    private final KeyboardKeyAvailabilityService availabilityService = new KeyboardKeyAvailabilityService();

    @Test
    void occupiedKeysAreCollectedFromEntireFileIncludingUnusedActions() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                    <UnusedByEliteIntel>
                        <Primary Device="Keyboard" Key="Key_L" />
                    </UnusedByEliteIntel>
                </Root>
                """);

        Set<String> occupied = availabilityService.occupiedKeyboardKeys(file);
        List<String> available = availabilityService.availableKeys(file, "GalaxyMapOpen", BindingSlotType.PRIMARY);

        assertTrue(occupied.contains("Key_L"));
        assertFalse(available.contains("Key_L"));
    }

    @Test
    void joystickHotasMouseAssignmentsDoNotOccupyKeyboardKeys() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <JoystickAction>
                        <Primary Device="044F0404" Key="Key_L" />
                        <Secondary Device="Mouse" Key="Key_M" />
                    </JoystickAction>
                </Root>
                """);

        Set<String> occupied = availabilityService.occupiedKeyboardKeys(file);
        List<String> available = availabilityService.availableKeys(file);

        assertFalse(occupied.contains("Key_L"));
        assertFalse(occupied.contains("Key_M"));
        assertTrue(available.contains("Key_L"));
        assertTrue(available.contains("Key_M"));
    }

    @Test
    void noDeviceBlankAndKeyPlaceholderDoNotOccupyKeys() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <EmptyAction>
                        <Primary Device="{NoDevice}" Key="Key_L" />
                        <Secondary Device="Keyboard" Key="" />
                    </EmptyAction>
                    <PlaceholderAction>
                        <Primary Device="Keyboard" Key="Key_" />
                        <Secondary Device="Keyboard" Key="{NoDevice}" />
                    </PlaceholderAction>
                </Root>
                """);

        Set<String> occupied = availabilityService.occupiedKeyboardKeys(file);

        assertFalse(occupied.contains("Key_L"));
        assertFalse(occupied.contains("Key_"));
        assertFalse(occupied.contains("{NoDevice}"));
    }

    @Test
    void assigningOccupiedKeyReturnsKeyOccupied() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                    <UnusedByEliteIntel>
                        <Primary Device="Keyboard" Key="Key_L" />
                    </UnusedByEliteIntel>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_L"));

        assertEquals(BindingSaveResult.KEY_OCCUPIED, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void assigningUnknownKeyReturnsUnknownKey() throws Exception {
        Path file = writeBinds(minimalBinds());

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_NotReal"));

        assertEquals(BindingSaveResult.UNKNOWN_KEY, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void staleTimestampOrSizeReturnsStaleFile() throws Exception {
        Path file = writeBinds(minimalBinds());
        KeyboardBindingEdit staleEdit = edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M");
        Files.writeString(file, minimalBinds() + "\n<!-- changed by game -->", StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(staleEdit);

        assertEquals(BindingSaveResult.STALE_FILE, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void staleSizeMismatchReturnsStaleFile() throws Exception {
        Path file = writeBinds(minimalBinds());
        KeyboardBindingEdit edit = new KeyboardBindingEdit(
                file,
                "GalaxyMapOpen",
                BindingSlotType.PRIMARY,
                "Key_M",
                Files.getLastModifiedTime(file),
                Files.size(file) + 1
        );

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(edit);

        assertEquals(BindingSaveResult.STALE_FILE, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void staleTimestampMismatchReturnsStaleFile() throws Exception {
        Path file = writeBinds(minimalBinds());
        KeyboardBindingEdit edit = edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M");
        Files.setLastModifiedTime(file, FileTime.fromMillis(edit.expectedLastModified().toMillis() + 5_000));

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(edit);

        assertEquals(BindingSaveResult.STALE_FILE, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void backupIsCreatedBeforeWriteAndFilenameDoesNotEndWithBinds() throws Exception {
        Path file = writeBinds(minimalBinds());
        String before = Files.readString(file, StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter(
                availabilityService,
                fixedBackupService()
        ).assignKeyboardKey(edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        List<Path> backups = backups(file);
        assertEquals(BindingSaveResult.SAVED, result);
        assertEquals(1, backups.size());
        assertFalse(backups.get(0).getFileName().toString().endsWith(".binds"));
        assertEquals(before, Files.readString(backups.get(0), StandardCharsets.UTF_8));
    }

    @Test
    void backupFilenameCollisionCreatesSuffixedBackup() throws Exception {
        Path file = writeBinds(minimalBinds());
        BindingsBackupService backupService = fixedBackupService();
        Path firstBackup = backupService.createBackup(file);

        BindingSaveResult result = new BindingsWriter(
                availabilityService,
                backupService
        ).assignKeyboardKey(edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        List<Path> backups = backups(file);
        assertEquals(BindingSaveResult.SAVED, result);
        assertEquals(2, backups.size());
        assertTrue(Files.exists(firstBackup));
        assertTrue(backups.stream().anyMatch(path -> path.getFileName().toString().endsWith("-1.bak")));
        assertFalse(backups.stream().anyMatch(path -> path.getFileName().toString().endsWith(".binds")));
    }

    @Test
    void writerChangesOnlySelectedSlotAndPreservesOtherXml() throws Exception {
        String original = """
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_G">
                            <Modifier Device="Keyboard" Key="Key_LeftShift" />
                        </Primary>
                        <Secondary Device="045E028E" Key="Joy_1" />
                    </GalaxyMapOpen>
                    <UnusedByEliteIntel custom="kept">
                        <Primary Device="Keyboard" Key="Key_L" />
                        <UnknownChild value="preserved" />
                    </UnusedByEliteIntel>
                </Root>
                """;
        Path file = writeBinds(original);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(BindingSaveResult.SAVED, result);
        assertTrue(updated.contains("<Primary Device=\"Keyboard\" Key=\"Key_M\" />"));
        assertTrue(updated.contains("<Secondary Device=\"045E028E\" Key=\"Joy_1\" />"));
        assertTrue(updated.contains("<UnusedByEliteIntel custom=\"kept\">"));
        assertTrue(updated.contains("<UnknownChild value=\"preserved\" />"));
        assertFalse(updated.contains("<Modifier Device=\"Keyboard\" Key=\"Key_LeftShift\" />"));
    }

    @Test
    void crlfLineEndingsArePreservedOutsideSelectedSlot() throws Exception {
        String original = String.join("\r\n",
                "<Root>",
                "    <GalaxyMapOpen>",
                "        <Primary Device=\"{NoDevice}\" Key=\"\" />",
                "        <Secondary Device=\"045E028E\" Key=\"Joy_1\" />",
                "    </GalaxyMapOpen>",
                "    <Unrelated value=\"kept\" />",
                "</Root>",
                "");
        Path file = writeBinds(original);
        String expected = original.replace(
                "<Primary Device=\"{NoDevice}\" Key=\"\" />",
                "<Primary Device=\"Keyboard\" Key=\"Key_M\" />");

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.SAVED, result);
        assertEquals(expected, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void utf8BomIsPreservedOnWrite() throws Exception {
        Path file = tempDir.resolve("Test.4.1.binds");
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = minimalBinds().getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, withBom, 0, bom.length);
        System.arraycopy(content, 0, withBom, bom.length, content.length);
        Files.write(file, withBom);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        byte[] updatedPrefix = new byte[3];
        System.arraycopy(Files.readAllBytes(file), 0, updatedPrefix, 0, 3);
        assertEquals(BindingSaveResult.SAVED, result);
        assertArrayEquals(bom, updatedPrefix);
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("Key=\"Key_M\""));
    }

    @Test
    void selectedNonKeyboardSlotCanBeReplacedWhenThatExactSlotIsSelected() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_G" />
                        <Secondary Device="045E028E" Key="Joy_1" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.SECONDARY, "Key_M"));

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(BindingSaveResult.SAVED, result);
        assertTrue(updated.contains("<Primary Device=\"Keyboard\" Key=\"Key_G\" />"));
        assertTrue(updated.contains("<Secondary Device=\"Keyboard\" Key=\"Key_M\" />"));
        assertFalse(updated.contains("<Secondary Device=\"045E028E\" Key=\"Joy_1\" />"));
    }

    @Test
    void clearingKeyboardSlotWritesNoDeviceBlankAndPreservesOtherSlot() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_G" />
                        <Secondary Device="Keyboard" Key="Key_H" />
                    </GalaxyMapOpen>
                    <Unrelated value="kept" />
                </Root>
                """);
        String before = Files.readString(file, StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, null));

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        List<Path> backups = backups(file);
        assertEquals(BindingSaveResult.SAVED, result);
        assertTrue(updated.contains("<Primary Device=\"{NoDevice}\" Key=\"\" />"));
        assertTrue(updated.contains("<Secondary Device=\"Keyboard\" Key=\"Key_H\" />"));
        assertTrue(updated.contains("<Unrelated value=\"kept\" />"));
        assertEquals(1, backups.size());
        assertEquals(before, Files.readString(backups.get(0), StandardCharsets.UTF_8));
    }

    @Test
    void clearingNonKeyboardSlotWritesNoDeviceBlankAndPreservesOtherSlot() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_G" />
                        <Secondary Device="045E028E" Key="Joy_1" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.SECONDARY, null));

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(BindingSaveResult.SAVED, result);
        assertTrue(updated.contains("<Primary Device=\"Keyboard\" Key=\"Key_G\" />"));
        assertTrue(updated.contains("<Secondary Device=\"{NoDevice}\" Key=\"\" />"));
        assertFalse(updated.contains("<Secondary Device=\"045E028E\" Key=\"Joy_1\" />"));
    }

    @Test
    void clearingAlreadyNoDeviceBlankSlotIsNoChange() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="Keyboard" Key="Key_H" />
                    </GalaxyMapOpen>
                </Root>
                """);
        String before = Files.readString(file, StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, null));

        assertEquals(BindingSaveResult.NO_CHANGE, result);
        assertEquals(before, Files.readString(file, StandardCharsets.UTF_8));
        assertEquals(0, backups(file).size());
    }

    @Test
    void staleClearReturnsStaleFileWithoutBackupOrWrite() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_G" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);
        KeyboardBindingEdit staleEdit = edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, null);
        Files.writeString(file, Files.readString(file, StandardCharsets.UTF_8) + "\n<!-- changed by game -->", StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(staleEdit);

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(BindingSaveResult.STALE_FILE, result);
        assertTrue(updated.contains("<Primary Device=\"Keyboard\" Key=\"Key_G\" />"));
        assertTrue(updated.contains("changed by game"));
        assertEquals(0, backups(file).size());
    }

    @Test
    void noChangeDoesNotCreateBackupOrWrite() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="Keyboard" Key="Key_M" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);
        String before = Files.readString(file, StandardCharsets.UTF_8);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.NO_CHANGE, result);
        assertEquals(before, Files.readString(file, StandardCharsets.UTF_8));
        assertEquals(0, backups(file).size());
    }

    @Test
    void missingBindingReturnsBindingNotFound() throws Exception {
        Path file = writeBinds(minimalBinds());

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "DoesNotExist", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.BINDING_NOT_FOUND, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void duplicateActionNodeReturnsUnsupportedXml() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.UNSUPPORTED_XML, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void selectedSlotNotFoundReturnsUnsupportedXml() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.SECONDARY, "Key_M"));

        assertEquals(BindingSaveResult.UNSUPPORTED_XML, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void ambiguousUnsupportedXmlReturnsUnsupportedXml() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.UNSUPPORTED_XML, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void unknownSelectedSlotAttributeReturnsUnsupportedXml() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" Mystery="keep-me" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.UNSUPPORTED_XML, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void unknownSelectedSlotChildReturnsUnsupportedXml() throws Exception {
        Path file = writeBinds("""
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="">
                            <Unexpected value="keep-me" />
                        </Primary>
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """);

        BindingSaveResult result = new BindingsWriter().assignKeyboardKey(
                edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        assertEquals(BindingSaveResult.UNSUPPORTED_XML, result);
        assertEquals(0, backups(file).size());
    }

    @Test
    void staleAfterBackupReturnsStaleFileWithoutWritingReplacement() throws Exception {
        Path file = writeBinds(minimalBinds());
        BindingsBackupService mutatingBackup = new BindingsBackupService() {
            @Override
            public Path createBackup(Path bindsFile) throws IOException {
                Path backup = super.createBackup(bindsFile);
                Files.writeString(bindsFile, minimalBinds() + "\n<!-- changed by game -->", StandardCharsets.UTF_8);
                return backup;
            }
        };

        BindingSaveResult result = new BindingsWriter(
                availabilityService,
                mutatingBackup
        ).assignKeyboardKey(edit(file, "GalaxyMapOpen", BindingSlotType.PRIMARY, "Key_M"));

        String updated = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(BindingSaveResult.STALE_FILE, result);
        assertTrue(updated.contains("changed by game"));
        assertFalse(updated.contains("Key=\"Key_M\""));
    }

    private KeyboardBindingEdit edit(
            Path file,
            String bindingId,
            BindingSlotType slotType,
            String key
    ) throws IOException {
        return new KeyboardBindingEdit(
                file,
                bindingId,
                slotType,
                key,
                Files.getLastModifiedTime(file),
                Files.size(file)
        );
    }

    private Path writeBinds(String xml) throws IOException {
        Path file = tempDir.resolve("Test.4.1.binds");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        return file;
    }

    private String minimalBinds() {
        return """
                <Root>
                    <GalaxyMapOpen>
                        <Primary Device="{NoDevice}" Key="" />
                        <Secondary Device="{NoDevice}" Key="" />
                    </GalaxyMapOpen>
                </Root>
                """;
    }

    private List<Path> backups(Path bindsFile) throws IOException {
        try (var files = Files.list(bindsFile.getParent())) {
            return files
                    .filter(path -> path.getFileName().toString().contains(".elite-intel-backup-"))
                    .toList();
        }
    }

    private BindingsBackupService fixedBackupService() {
        return new BindingsBackupService(Clock.fixed(Instant.parse("2026-06-02T12:00:00Z"), ZoneOffset.UTC));
    }
}
