package elite.intel.session.ui;

public enum CentreTab implements PanelTab {

    COMMANDER(0), FIGHTER(1), SRV(2), CREW(3);

    public final int index;

    CentreTab(int tabIndex) {
        this.index = tabIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
