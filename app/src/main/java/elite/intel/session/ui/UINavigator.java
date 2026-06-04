package elite.intel.session.ui;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import elite.intel.ai.hands.Bindings;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.StatusFlags.GuiFocus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Dispatches panel navigation keystrokes on behalf of c
 * command handlers.
 * <p>
 * Owns no state itself - all panel/tab tracking lives in PanelStateTracker.
 * Command handlers instantiate this with their CommandOperator and call
 * openAndNavigate() / closeAndRestore(). Everything else is handled here.
 */
public class UINavigator {

    private static final Logger log = LogManager.getLogger(UINavigator.class);
    private final Status status = Status.getInstance();
    // How many times to blindly press NextPanel to guarantee a full wrap
    // back to index 0 on each panel. One full tab-count is always sufficient.
    private static final int LEFT_PANEL_TAB_COUNT = LeftPanel.values().length;
    private static final int COMMS_PANEL_TAB_COUNT = CommsPanel.values().length;
    private static final int CENTRE_PANEL_TAB_COUNT = CenterPanel.values().length;
    private static final int RIGHT_PANEL_TAB_COUNT = RightPanel.values().length;

    private static final int RANDOM_MIN = 99;
    private static final int RANDOM_MAX = 201;
    private static final int PANEL_OPEN_POLL_MS = 50;   // how often to check GuiFocus after sending open key
    private static final int PANEL_OPEN_TIMEOUT_MS = 3000; // per-attempt timeout before concluding the key was missed
    private static final int PANEL_OPEN_MAX_ATTEMPTS = 3;  // retry limit before giving up

    private final PanelStateTracker tracker = PanelStateTracker.getInstance();
    private final GlobalSettingsManager globalSettingsManager = GlobalSettingsManager.getInstance();

    public UINavigator() {
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
            for (int attempt = 1; attempt <= PANEL_OPEN_MAX_ATTEMPTS; attempt++) {
                openPanel(panel);
                if (waitForPanel(panel)) break;
                log.warn("Panel {} did not open after {}ms (attempt {}/{}), retrying",
                        panel, PANEL_OPEN_TIMEOUT_MS, attempt, PANEL_OPEN_MAX_ATTEMPTS);
            }
        }

        if (!state.isKnown()) {
            blindResetToDefault(panel);
        }

        navigateToTargetTab(panel, target);
        inputDelay(250); // <--- important! Don't be hasty.
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


        StatusFlags.GuiFocus lastOpened = tracker.getLastOpenedPanel();
        if (lastOpened != null) {
            closeAndRestore(lastOpened);
        }

        if (status.isFssModeActive()) {

            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(Bindings.GameCommand.BINDING_EXPLORATION_FSSQUIT.getGameBinding())));
        }

        if (status.isSaaModeActive()) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(Bindings.GameCommand.EXPLORATION_SAAEXIT_THIRD_PERSON.getGameBinding())));
        }

        if (shouldBackOut()) {
            for (int i = 0; i < 10; i++) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(Bindings.GameCommand.BINDING_EXIT_KEY.getGameBinding())));
            }
        } else {
            for (int i = 0; i < 3; i++) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(Bindings.GameCommand.BINDING_EXIT_KEY.getGameBinding())));
            }
        }
    }

    private boolean shouldBackOut() {
        if (!globalSettingsManager.getAutoExitUiBeforeOpeningAnotherWindow()) return false;
        if (status.isGalaxyMapOpen()) return true;
        if (status.isSystemMapOpen()) return true;
        if (status.isOrreryOpen()) return true;
        if (status.isFssModeActive()) return true;
        if (status.isSaaModeActive()) return true;
        if (status.isStationServicesOpen()) return true;
        return status.isCodexOpen();
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
        inputDelay(RANDOM_MAX);
        tracker.notifyEliteIntelClosedPanel();
        state.resetToDefault();
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
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(key))); // always tap - binding.hold flag must be ignored for tab cycling
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
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(key))); // always tap - binding.hold flag must be ignored for tab cycling
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
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(nextTab))); // always tap
        }
        state.resetToDefault();
    }

    /**
     * Polls {@link Status#getGuiFocus()} until the panel opens or the timeout expires.
     *
     * @return true if the panel opened, false if the timeout was reached (key was likely missed)
     */
    private boolean waitForPanel(GuiFocus panel) {
        long deadline = System.currentTimeMillis() + PANEL_OPEN_TIMEOUT_MS;
        while (status.getGuiFocus() != panel && System.currentTimeMillis() < deadline) {
            sleep(PANEL_OPEN_POLL_MS);
        }
        return status.getGuiFocus() == panel;
    }

    private void openPanel(GuiFocus panel) {
        Status status = Status.getInstance();
        String binding = null;
        if (status.isInSrv()) {
            binding = switch (panel) {
                case EXTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL_BUGGY.getGameBinding();
                case COMMS_PANEL -> Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL_BUGGY.getGameBinding();
                case INTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding();
                case ROLE_PANEL -> Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL_BUGGY.getGameBinding();
                default -> throw new IllegalArgumentException("No panel binding for GuiFocus: " + panel);
            };
        } else if (status.isInMainShip() || status.isInFighter()) {
            binding = switch (panel) {
                case EXTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL.getGameBinding();
                case COMMS_PANEL -> Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL.getGameBinding();
                case INTERNAL_PANEL -> Bindings.GameCommand.BINDING_FOCUS_INTERNAL_PANEL.getGameBinding();
                case ROLE_PANEL -> Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding();
                default -> throw new IllegalArgumentException("No panel binding for GuiFocus: " + panel);
            };
        }
        if (binding != null) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingTap(binding), // panel focus keys must be tapped, not held
                    GameInputStep.delay(RANDOM_MAX)
            ));
        }
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


    public static int randomDelay() {
        return RANDOM_MIN + ThreadLocalRandom.current().nextInt(RANDOM_MAX - RANDOM_MIN + 1);
    }

    private void inputDelay(int delayMs) {
        GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.delay(delayMs)));
    }
}
