package elite.intel.session.ui;

import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.session.StatusFlags;
import elite.intel.session.StatusFlags.GuiFocus;

/**
 * Dispatches panel navigation keystrokes on behalf of c
 * command handlers.
 * <p>
 * Owns no state itself — all panel/tab tracking lives in PanelStateTracker.
 * Command handlers instantiate this with their CommandOperator and call
 * openAndNavigate() / closeAndRestore(). Everything else is handled here.
 */
public class UINavigator {

    // How many times to blindly press NextPanel to guarantee a full wrap
    // back to index 0 on each panel. One full tab-count is always sufficient.
    private static final int LEFT_PANEL_TAB_COUNT = LeftPanelTab.values().length;
    private static final int COMMS_PANEL_TAB_COUNT = CommsTab.values().length;
    private static final int CENTRE_PANEL_TAB_COUNT = CentreTab.values().length;
    private static final int RIGHT_PANEL_TAB_COUNT = RightPanelTab.values().length;

    // Minimum sleep between tab keypresses. The game needs time to register
    // each tab change before the next keystroke fires — without this, rapid
    // multi-step navigation skips tabs and the tracker diverges from game state.
    private static final int TAB_DELAY_MS = 1500;
    // Sleep after opening or closing a panel — the game needs time to render
    // the panel before the first tab keystroke fires, and time to process the
    // close before the caller's next action.
    private static final int PANEL_OPEN_DELAY_MS = 1500;

    private final CommandOperator operator;
    private final PanelStateTracker tracker = PanelStateTracker.getInstance();

    public UINavigator(CommandOperator operator) {
        this.operator = operator;
    }

    // -------------------------------------------------------------------------
    // Public API for command handlers
    // -------------------------------------------------------------------------

    /**
     * Open a panel, navigate to the target tab, and leave it open.
     * Saves the current tab as the restore point for a subsequent closeAndRestore().
     * If the tab position is unknown (player moved it), performs a blind reset first.
     */
    public void openAndNavigate(GuiFocus panel, PanelTab target, int delayMs) {
        PanelState<?> state = tracker.getState(panel);
        state.beginCommand();

        tracker.notifyEliteIntelOpeningPanel(panel);
        openPanel(panel, delayMs);
        sleep(PANEL_OPEN_DELAY_MS);

        if (!state.isKnown()) {
            blindResetToDefault(panel, delayMs);
        }

        navigateToTab(panel, target, delayMs);
    }


    public void closeOpenPanel() {
        StatusFlags.GuiFocus panel = PanelStateTracker.getInstance().getLastOpenedPanel();
        if (panel != null) {
            closeAndRestore(panel, 0);
        }
    }


    /**
     * Navigate back to the tab that was active before the last openAndNavigate(),
     * then close the panel. If no restore point exists, navigates to default first.
     */
    public void closeAndRestore(GuiFocus panel, int delayMs) {
        PanelState<?> state = tracker.getState(panel);
        PanelTab restore = state.getBeforeCommand();

        if (state.isKnown()) {
            if (restore != null) {
                navigateToTab(panel, restore, delayMs);
            } else {
                navigateToTab(panel, state.getDefault(), delayMs);
            }
        }
        // If unknown we can't navigate back cleanly — just close.
        closePanel(panel, delayMs);
        sleep(PANEL_OPEN_DELAY_MS);
        tracker.notifyEliteIntelClosedPanel();
    }

    // -------------------------------------------------------------------------
    // Private navigation helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void navigateToTab(GuiFocus panel, PanelTab target, int delayMs) {
        PanelState state = tracker.getState(panel);
        int steps = state.stepsTo(target);

        if (steps == 0) return;

        // Positive = go right = BINDING_CYCLE_NEXT_PANEL
        // Negative = go left  = BINDING_CYCLE_PREVIOUS_PANEL
        // NOTE: Frontier inverted the tab direction bindings. Do not "fix" this.
        String key = steps > 0
                ? Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding()
                : Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding();

        for (int i = 0; i < Math.abs(steps); i++) {
            operator.operateKeyboardTap(key); // always tap — binding.hold flag must be ignored for tab cycling
            sleep(TAB_DELAY_MS);
        }
        // Cast is safe: each PanelState is always paired with its matching tab enum
        // by construction in PanelStateTracker's field declarations.
        state.recordTab((Enum & PanelTab) target);
    }

    /**
     * When tab position is unknown, spam BINDING_CYCLE_NEXT_PANEL enough times
     * to guarantee we've wrapped past every possible tab, landing at index 0.
     * Requires that the tab list wraps around in the game UI (it does).
     */
    @SuppressWarnings("rawtypes")
    private void blindResetToDefault(GuiFocus panel, int delayMs) {
        String nextTab = Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding();
        for (int i = 0; i < getTabCount(panel); i++) {
            operator.operateKeyboardTap(nextTab); // always tap
            sleep(TAB_DELAY_MS);
        }
        tracker.getState(panel).resetToDefault();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void openPanel(GuiFocus panel, int delayMs) {
        String binding = switch (panel) {
            case EXTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL.getGameBinding();
            case COMMS_PANEL -> Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL.getGameBinding();
            case INTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_INTERNAL_PANEL.getGameBinding();
            case ROLE_PANEL -> Bindings.GameCommand.BINDING_FOCUS_RADAR_PANEL.getGameBinding();
            default -> throw new IllegalArgumentException("No panel binding for GuiFocus: " + panel);
        };
        operator.operateKeyboardTap(binding); // panel focus keys must be tapped, not held
    }

    private void closePanel(GuiFocus panel, int delayMs) {
        // Pressing the panel's own open key again closes it
        openPanel(panel, delayMs);
    }

    private int getTabCount(GuiFocus panel) {
        return switch (panel) {
            case EXTERNAL_PANEL -> LEFT_PANEL_TAB_COUNT;
            case COMMS_PANEL -> COMMS_PANEL_TAB_COUNT;
            case ROLE_PANEL -> CENTRE_PANEL_TAB_COUNT;
            case INTERNAL_PANEL -> RIGHT_PANEL_TAB_COUNT;
            default -> throw new IllegalArgumentException("No tab count for GuiFocus: " + panel);
        };
    }
}