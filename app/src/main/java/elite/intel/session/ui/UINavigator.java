package elite.intel.session.ui;

import elite.intel.ai.brain.handlers.commands.Bindings;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.StatusFlags.GuiFocus;

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
    private static final int TAB_DELAY_MS = 120;
    // Sleep after opening or closing a panel - the game needs time to render
    // the panel before the first tab keystroke fires, and time to process the
    // close before the caller's next action.
    private static final int PANEL_OPEN_DELAY_MS = 120;

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
        if (tracker.getLastOpenedPanel() == panel) {
            // Panel already open - skip the close/open cycle and just move tabs
            navigateToTab(panel, target);
            return;
        }

        closeOpenPanel();
        PanelState<?> state = tracker.getState(panel);

        tracker.notifyEliteIntelOpeningPanel(panel);
        openPanel(panel);
        sleep(PANEL_OPEN_DELAY_MS);

        if (!state.isKnown()) {
            blindResetToDefault(panel);
        }

        navigateToTab(panel, target);
    }


    public void closeOpenPanel() {
        /// hack-workaround - clears any current UI selection
        operator.operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);

        StatusFlags.GuiFocus panel = PanelStateTracker.getInstance().getLastOpenedPanel();
        if (panel != null) {
            closeAndRestore(panel);
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
        if (state.isKnown()) {
            navigateToTab(panel, state.getDefault());
        }
        closePanel(panel);
        sleep(PANEL_OPEN_DELAY_MS);
        tracker.notifyEliteIntelClosedPanel();
    }

    // -------------------------------------------------------------------------
    // Private navigation helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void navigateToTab(GuiFocus panel, PanelTab target) {
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
            operator.operateKeyboardTap(key); // always tap - binding.hold flag must be ignored for tab cycled
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
    private void blindResetToDefault(GuiFocus panel) {
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