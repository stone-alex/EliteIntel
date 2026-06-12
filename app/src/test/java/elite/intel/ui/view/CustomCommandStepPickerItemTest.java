package elite.intel.ui.view;

import elite.intel.ai.brain.actions.catalog.CommandCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCommandStepPickerItemTest {

    @Test
    void resolvesStoredIdFromPickerItem() {
        CustomCommandStepPickerItem item = new CustomCommandStepPickerItem("deploy_landing_gear", "Deploy landing gear", true);

        assertEquals("deploy_landing_gear", CustomCommandStepPickerItem.resolveId(item));
    }

    @Test
    void resolvesStoredIdFromEditableDisplayText() {
        assertEquals("deploy_landing_gear", CustomCommandStepPickerItem.resolveId("Deploy landing gear - deploy_landing_gear"));
    }

    @Test
    void unknownItemKeepsLegacyIdVisibleAndResolvable() {
        CustomCommandStepPickerItem item = CustomCommandStepPickerItem.unknown("legacy_command", "Unknown command");

        assertEquals("legacy_command", CustomCommandStepPickerItem.resolveId(item));
        assertTrue(item.toString().contains("legacy_command"));
    }

    @Test
    void bindingItemsContainKnownEliteDangerousBindingIds() {
        assertTrue(CustomCommandStepPickerItem.bindingItems().stream()
                .anyMatch(item -> item.id().equals("LandingGearToggle")));
    }

    @Test
    void bindingItemsAreDeduplicatedById() {
        List<String> ids = CustomCommandStepPickerItem.bindingItems().stream()
                .map(CustomCommandStepPickerItem::id)
                .toList();

        assertEquals(ids.size(), ids.stream().distinct().count());
    }

    @Test
    void builtInCommandItemsDoNotExposeCustomCommandEntries() {
        CommandCatalog catalog = new CommandCatalog();
        //just for test 1
        assertFalse(catalog.entries().stream().anyMatch(entry -> entry.isCustomCommand()));
        assertEquals(catalog.entries().size(), CustomCommandStepPickerItem.builtInCommandItemPS D:\development\IdeaProjects\EliteIntel> git remote -v
        fork    https://github.com/Gnevko/EliteIntel.git (fetch)
        fork    https://github.com/Gnevko/EliteIntel.git (push)
        origin  https://github.com/SudoKrondor/EliteIntel.git (fetch)
        origin  https://github.com/SudoKrondor/EliteIntel.git (push)
        PS D:\development\IdeaProjects\EliteIntel> git branch --show-current
        V1.1
        PS D:\development\IdeaProjects\EliteIntel> git status
        On branch V1.1
        Your branch is ahead of 'origin/V1.1' by 1 commit.
        (use "git push" to publish your local commits)

        nothing to commit, working tree clean
        PS D:\development\IdeaProjects\EliteIntel> git push
        Enumerating objects: 21, done.
                Counting objects: 100% (21/21), done.
                Delta compression using up to 16 threads
        Compressing objects: 100% (7/7), done.
                Writing objects: 100% (11/11), 724 bytes | 724.00 KiB/s, done.
                Total 11 (delta 4), reused 7 (delta 0), pack-reused 0 (from 0)
        remote: Resolving deltas: 100% (4/4), completed with 4 local objects.
        remote: Bypassed rule violations for refs/heads/V1.1:
        remote:
        remote: - Changes must be made through a pull request.
                remote:
        remote: - Required status check "- - CI / build-windows" is expected.
        remote:
        To https://github.com/SudoKrondor/EliteIntel.git
        9dfb5ca2806..2209199dcdc  V1.1 -> V1.1
        PS D:\development\IdeaProjects\EliteIntel> s().size());
    }
}
