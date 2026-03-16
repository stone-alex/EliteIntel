package elite.intel.session.ui;

public enum LeftPanel implements PanelTab {
    SYSTEM(0),
    NAVIGATION(1), // <-- default at game start
    TRANSACTIONS(2),
    CONTACTS(3);

    public final int index;

    LeftPanel(int tabIndex) {
        this.index = tabIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }
}