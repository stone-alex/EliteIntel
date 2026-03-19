package elite.intel.session.ui;

import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.StatusFlags.GuiFocus;
import elite.intel.util.SleepNoThrow;

/**
 * Dispatches panel navigation keystrokes on behalf of c
 * command handlers.
 * <p>
 * Owns no state itself - all panel/tab tracking lives in PanelStateTracker.
 * Command handlers instantiate this with their CommandOperator and call
 * openAndNavigate() / closeAndRestore(). Everything else is handled here.
 */
public class UINavigator {

    private final Status status = Status.getInstance();
    // How many times to blindly press NextPanel to guarantee a full wrap
    // back to index 0 on each panel. One full tab-count is always sufficient.
    private static final int LEFT_PANEL_TAB_COUNT = LeftPanel.values().length;
    private static final int COMMS_PANEL_TAB_COUNT = CommsPanel.values().length;
    private static final int CENTRE_PANEL_TAB_COUNT = CenterPanel.values().length;
    private static final int RIGHT_PANEL_TAB_COUNT = RightPanel.values().length;

    // Minimum sleep between tab keypresses. The game needs time to register
    // each tab change before the next keystroke fires - without this, rapid
    // multi-step navigation skips tabs and the tracker diverges from game state.
    private static final int TAB_DELAY_MS = 150;
    // Sleep after opening or closing a panel - the game needs time to render
    // the panel before the first tab keystroke fires, and time to process the
    // close before the caller's next action.
    private static final int PANEL_OPEN_DELAY_MS = 250;

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
     * If the panel is already open, just navigates to the target tab without closing and reopening.
     * If the tab position is unknown (player moved it), performs a blind reset first.
     */
    public void openAndNavigate(GuiFocus panel, PanelTab target) {
        // Check tracker BEFORE closing - if we already have this panel open, just navigate.
        if (tracker.getLastOpenedPanel() == panel) {
            navigateToTargetTab(panel, target);
            return;
        }

        closeOpenPanel();

        PanelState<?> state = tracker.getState(panel);
        if (state == null) {
            return;
        }

        tracker.notifyEliteIntelOpeningPanel(panel);
        // Only send the open keystroke if the game doesn't already have this panel open.
        // openPanel() is a toggle - sending it when the panel is already open would close it.
        if (status.getGuiFocus() != panel) {
            openPanel(panel);
            sleep(PANEL_OPEN_DELAY_MS);
        }

        if (!state.isKnown()) {
            blindResetToDefault(panel);
        }

        navigateToTargetTab(panel, target);
        SleepNoThrow.sleep(250); // <--- important! Don't be hasty.
    }


    /**
     * Tells the tracker that the game has already reset a panel to its default tab
     * without any keystroke from us - e.g. when the game switches context on SRV
     * deploy/recover. No keystrokes are sent; this is purely a state sync.
     */
    public void assumeDefaultState(GuiFocus panel) {
        PanelState<?> state = tracker.getState(panel);
        if (state != null) {
            state.resetToDefault();
        }
    }

    public void closeOpenPanel() {
        /// hack-workaround - clears any current UI selection
        operator.operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);

        StatusFlags.GuiFocus lastOpened = tracker.getLastOpenedPanel();
        if (lastOpened != null) {
            closeAndRestore(lastOpened);
        } else {
            // Tracker has no record of an open panel, but the game may have one open
            // (e.g. player opened it manually). Close it via toggle to avoid the
            // subsequent openPanel() call toggling it shut instead of open.
            StatusFlags.GuiFocus currentFocus = status.getGuiFocus();
            if (currentFocus != null && tracker.getState(currentFocus) != null) {
                closePanel(currentFocus);
                sleep(PANEL_OPEN_DELAY_MS);
            }
        }

        if (status.isFssModeActive()) {
            operator.operateKeyboard(Bindings.GameCommand.BINDING_EXPLORATION_FSSQUIT.getGameBinding(), 0);
        }

        if (status.isGalaxyMapOpen() || status.isSystemMapOpen() || status.isSaaModeActive()) {
            /// traverse out of all nested windows
            for (int i = 0; i < 10; i++) {
                operator.operateKeyboard(Bindings.GameCommand.BINDING_EXIT_KEY.getGameBinding(), 0);
            }
        }
    }


    /**
     * Navigate to the panel's default tab, then close it.
     */
    public void closeAndRestore(GuiFocus panel) {
        PanelState<?> state = tracker.getState(panel);
        if (state == null) {
            return;
        }
        if (state.isKnown()) {
            navigateToDefaultTab(panel, state.getDefault());
        }
        closePanel(panel);
        sleep(PANEL_OPEN_DELAY_MS);
        tracker.notifyEliteIntelClosedPanel();
    }

    // -------------------------------------------------------------------------
    // Private navigation helpers
    // -------------------------------------------------------------------------

    // Always navigate to target by going right (NEXT_PANEL).
    // NOTE: Frontier inverted the tab direction bindings. Do not "fix" this.
    @SuppressWarnings({"unchecked", "rawtypes"})

    private void navigateToTargetTab(GuiFocus panel, PanelTab target) {
        PanelState state = tracker.getState(panel);
        if (state == null) {
            return;
        }
        int steps = state.stepsToRight(target);
        if (steps == 0) return;
        String key;
        if (target instanceof CenterPanel) {
            key = Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding();
        } else {
            key = Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding();
        }

        for (int i = 0; i < steps; i++) {
            operator.operateKeyboardTap(key); // always tap - binding.hold flag must be ignored for tab cycling
            sleep(TAB_DELAY_MS);
        }
        state.recordTab((Enum & PanelTab) target);
    }

    // Always navigate to default by going left (PREVIOUS_PANEL).
    // NOTE: Frontier inverted the tab direction bindings. Do not "fix" this.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void navigateToDefaultTab(GuiFocus panel, PanelTab defaultTarget) {
        PanelState state = tracker.getState(panel);
        if (state == null) {
            return;
        }
        int steps = state.stepsToLeft(defaultTarget);
        if (steps == 0) return;
        String key = Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding();
        for (int i = 0; i < steps; i++) {
            operator.operateKeyboardTap(key); // always tap - binding.hold flag must be ignored for tab cycling
            sleep(TAB_DELAY_MS);
        }
        state.recordTab((Enum & PanelTab) defaultTarget);
    }

    /**
     * When tab position is unknown, spam BINDING_CYCLE_NEXT_PANEL enough times
     * to guarantee we've wrapped past every possible tab, landing at index 0.
     * Requires that the tab list wraps around in the game UI (it does).
     */
    @SuppressWarnings("rawtypes")
    private void blindResetToDefault(GuiFocus panel) {
        PanelState<?> state = tracker.getState(panel);
        if (state == null) {
            return;
        }
        String nextTab = Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding();
        for (int i = 0; i < getTabCount(panel); i++) {
            operator.operateKeyboardTap(nextTab); // always tap
            sleep(TAB_DELAY_MS);
        }
        state.resetToDefault();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void openPanel(GuiFocus panel) {
        Status status = Status.getInstance();
        String binding = null;
        if (status.isInSrv()) {
            binding = switch (panel) {
                case EXTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL_BUGGY.getGameBinding();
                case COMMS_PANEL -> Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL_BUGGY.getGameBinding();
                case INTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding();
                case ROLE_PANEL -> Bindings.GameCommand.BINDING_FOCUS_RADAR_PANEL_BUGGY.getGameBinding();
                default -> throw new IllegalArgumentException("No panel binding for GuiFocus: " + panel);
            };
        } else if (status.isInMainShip()) {
            binding = switch (panel) {
                case EXTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL.getGameBinding();
                case COMMS_PANEL -> Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL.getGameBinding();
                case INTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_INTERNAL_PANEL.getGameBinding();
                case ROLE_PANEL -> Bindings.GameCommand.BINDING_FOCUS_RADAR_PANEL.getGameBinding();
                default -> throw new IllegalArgumentException("No panel binding for GuiFocus: " + panel);
            };
        }
        if (binding != null) operator.operateKeyboardTap(binding); // panel focus keys must be tapped, not held
    }

    private void closePanel(GuiFocus panel) {
        // Pressing the panel's own open key again closes it
        openPanel(panel);
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