package elite.intel.session;

import elite.intel.gameapi.journal.events.dto.LocationDto;

public class HomeSystem  extends SessionPersistence implements java.io.Serializable {

    private static final String DIRECTORY = "session/";
    private static final String HOME_SYSTEM = "homeSystem";
    private LocationDto locationDto;
    private static volatile HomeSystem instance;

    public static HomeSystem getInstance() {
        if(instance == null) {
            synchronized (HomeSystem.class) {
                if(instance == null) {
                    instance = new HomeSystem(DIRECTORY);
                }
            }
        }
        return instance;
    }


    private HomeSystem(String directory) {
        super(directory);
        ensureFileAndDirectoryExist("homeSystem.json");

        registerField(HOME_SYSTEM, this::getHomeSystem, this::setHomeSystem, LocationDto.class);
        loadFromDisk();
    }


    public LocationDto getHomeSystem() {
        return locationDto;
    }

    public void setHomeSystem(LocationDto locationDto) {
        this.locationDto = locationDto;
        save();
    }


    private void loadFromDisk() {
        loadSession(HomeSystem.this::loadFields);
    }
}
