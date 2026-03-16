package elite.intel.session.ui;

public enum CommsPanel implements PanelTab {
    CHAT(0), INBOX(1), SOCIAL(2), HISTORY(3), SQUADRON(4), CHANNELS(5);


    public final int index;

    CommsPanel(int tabIndex) {
        this.index = tabIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }
}