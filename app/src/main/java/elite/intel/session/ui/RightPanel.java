package elite.intel.session.ui;

public enum RightPanel implements PanelTab {

    MAIN(0), MODULES(1), FIRE_GROUPS(2), SHIP(3), INVENTORY(4), STORAGE(5), STATUS(6), PLAYLIST(7);

    public final int index;

    RightPanel(int tabIndex) {
        this.index = tabIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }

}