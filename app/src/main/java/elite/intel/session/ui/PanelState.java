package elite.intel.session.ui;

public class PanelState<T extends Enum<T> & PanelTab> {

    private final T defaultTab;
    private T currentTab;
    private T tabBeforeCommand; // saved restore point for "Close" backstep
    private boolean known;

    public PanelState(T defaultTab) {
        this.defaultTab = defaultTab;
        this.currentTab = defaultTab;
        this.known = true;
    }

    /**
     * Called by UINavigator before issuing a navigation command — saves restore point for Close.
     */
    public void beginCommand() {
        tabBeforeCommand = currentTab;
    }

    /**
     * Called by UINavigator after successfully navigating to a tab.
     */
    public void recordTab(T tab) {
        currentTab = tab;
        known = true;
    }

    /**
     * Called when the player opened this panel themselves — tab position is now unknown.
     */
    public void markUnknown() {
        known = false;
        tabBeforeCommand = null;
    }

    /**
     * Reset to default tab — called after a blind reset sequence.
     */
    public void resetToDefault() {
        currentTab = defaultTab;
        known = true;
    }

    public boolean isKnown() {
        return known;
    }

    public PanelTab getCurrent() {
        return currentTab;
    }

    public PanelTab getBeforeCommand() {
        return tabBeforeCommand;
    }

    public PanelTab getDefault() {
        return defaultTab;
    }

    /**
     * Returns the number of tab presses needed to reach the target from the current position.
     * Positive = BINDING_CYCLE_PREVIOUS_PANEL (moves right — Frontier naming is inverted).
     * Negative = BINDING_CYCLE_NEXT_PANEL     (moves left  — Frontier naming is inverted).
     * NOTE: Do not "fix" this inversion. It matches the actual in-game behaviour.
     */
    public int stepsTo(PanelTab target) {
        if (!known) throw new IllegalStateException(
                "Tab position unknown for panel — call resetToDefault() before navigating");
        return target.getIndex() - currentTab.getIndex();
    }

    /**
     * Returns the number of tab presses needed to reach the default tab from current position.
     * Used by UINavigator when closing without a saved restore point.
     */
    public int stepsToDefault() {
        return stepsTo(defaultTab);
    }
}