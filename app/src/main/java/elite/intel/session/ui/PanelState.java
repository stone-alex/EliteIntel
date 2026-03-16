package elite.intel.session.ui;

public class PanelState<T extends Enum<T> & PanelTab> {

    private final T defaultTab;
    private final int totalTabs;
    private T currentTab;
    private boolean known;

    public PanelState(T defaultTab) {
        this.defaultTab = defaultTab;
        this.currentTab = defaultTab;
        this.known = true;
        this.totalTabs = defaultTab.getDeclaringClass().getEnumConstants().length;
    }

    /**
     * Called by UINavigator after successfully navigating to a tab.
     */
    public void recordTab(T tab) {
        currentTab = tab;
        known = true;
    }

    /**
     * Called when the player opened this panel themselves - tab position is now unknown.
     */
    public void markUnknown() {
        known = false;
    }

    /**
     * Reset to default tab - called after a blind reset sequence.
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

    public PanelTab getDefault() {
        return defaultTab;
    }

    /**
     * Returns the number of tab presses needed to reach the target from the current position,
     * always choosing the shortest path (wraps around if that is fewer presses).
     * Positive = BINDING_CYCLE_PREVIOUS_PANEL (moves right - Frontier naming is inverted).
     * Negative = BINDING_CYCLE_NEXT_PANEL     (moves left  - Frontier naming is inverted).
     * NOTE: Do not "fix" this inversion. It matches the actual in-game behaviour.
     */
    public int stepsTo(PanelTab target) {
        if (!known)
            throw new IllegalStateException("Tab position unknown for panel - call resetToDefault() before navigating");
        int diff = target.getIndex() - currentTab.getIndex();
        if (diff > totalTabs / 2) diff -= totalTabs;
        else if (diff < -(totalTabs / 2)) diff += totalTabs;
        return diff;
    }
}