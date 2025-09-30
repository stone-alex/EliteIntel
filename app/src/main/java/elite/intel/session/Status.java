package elite.intel.session;

import elite.intel.gameapi.gamestate.events.GameEvents;

public class Status extends SessionPersistence implements java.io.Serializable {

    private static final String SESSION_DIR = "session/";
    private static volatile Status instance; // Singleton instance
    private GameEvents.StatusEvent gameStatus = new GameEvents.StatusEvent();
    private Long lastStatusChange = null;

    public static Status getInstance() {
        if (instance == null) {
            synchronized (Status.class) {
                if (instance == null) {
                    instance = new Status("status.json");
                }
            }
        }
        return instance;
    }

    private Status(String fileName) {
        super(SESSION_DIR);
        ensureFileAndDirectoryExist(fileName);
        loadFromDisk();
        registerField("game_status", this::getStatus, this::setStatus, GameEvents.StatusEvent.class);
        registerField("last_change", this::getLastStatusChange, this::setLastStatusChange, Long.class);
    }

    public GameEvents.StatusEvent getStatus() {
        return this.gameStatus;
    }

    public void setStatus(GameEvents.StatusEvent event) {
        this.gameStatus = event;
        lastStatusChange = System.currentTimeMillis();
        save();
    }

    public Long getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(Long lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    private void loadFromDisk() {
        loadSession(Status.this::loadFields);
    }
}