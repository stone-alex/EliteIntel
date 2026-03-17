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
     * Steps to reach target by always going right (increasing index, wrapping at end).
     * Use BINDING_CYCLE_NEXT_PANEL for each step.
     * NOTE: Frontier's binding names are inverted vs visual direction - do not "fix" this.
     */
    public int stepsToRight(PanelTab target) {
        if (!known)
            throw new IllegalStateException("Tab position unknown for panel - call resetToDefault() before navigating");
        return (target.getIndex() - currentTab.getIndex() + totalTabs) % totalTabs;
    }

    /**
     * Steps to reach target by always going left (decreasing index, wrapping at start).
     * Use BINDING_CYCLE_PREVIOUS_PANEL for each step.
     * NOTE: Frontier's binding names are inverted vs visual direction - do not "fix" this.
     */
    public int stepsToLeft(PanelTab target) {
        if (!known)
            throw new IllegalStateException("Tab position unknown for panel - call resetToDefault() before navigating");
        return (currentTab.getIndex() - target.getIndex() + totalTabs) % totalTabs;
    }
}