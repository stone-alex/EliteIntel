package elite.intel.ui.view;

import elite.intel.ai.brain.actions.catalog.CommandCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroStepPickerItemTest {

    @Test
    void resolvesStoredIdFromPickerItem() {
        MacroStepPickerItem item = new MacroStepPickerItem("deploy_landing_gear", "Deploy landing gear", true);

        assertEquals("deploy_landing_gear", MacroStepPickerItem.resolveId(item));
    }

    @Test
    void resolvesStoredIdFromEditableDisplayText() {
        assertEquals("deploy_landing_gear", MacroStepPickerItem.resolveId("Deploy landing gear - deploy_landing_gear"));
    }

    @Test
    void unknownItemKeepsLegacyIdVisibleAndResolvable() {
        MacroStepPickerItem item = MacroStepPickerItem.unknown("legacy_command", "Unknown command");

        assertEquals("legacy_command", MacroStepPickerItem.resolveId(item));
        assertTrue(item.toString().contains("legacy_command"));
    }

    @Test
    void bindingItemsContainKnownEliteDangerousBindingIds() {
        assertTrue(MacroStepPickerItem.bindingItems().stream()
                .anyMatch(item -> item.id().equals("LandingGearToggle")));
    }

    @Test
    void bindingItemsAreDeduplicatedById() {
        List<String> ids = MacroStepPickerItem.bindingItems().stream()
                .map(MacroStepPickerItem::id)
                .toList();

        assertEquals(ids.size(), ids.stream().distinct().count());
    }

    @Test
    void builtInCommandItemsDoNotExposeMacroEntries() {
        CommandCatalog catalog = new CommandCatalog();

        assertFalse(catalog.entries().stream().anyMatch(entry -> entry.isMacro()));
        assertEquals(catalog.entries().size(), MacroStepPickerItem.builtInCommandItems().size());
    }
}
