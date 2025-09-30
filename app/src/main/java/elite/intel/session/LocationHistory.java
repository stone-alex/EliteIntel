package elite.intel.session;

import com.google.gson.reflect.TypeToken;
import elite.intel.gameapi.journal.events.dto.LocationDto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationHistory extends SessionPersistence implements java.io.Serializable {
    private static final String HISTORY_DIR = "locations/";

    public static LocationHistory getInstance(String systemName) {
        return new LocationHistory(systemName+".json");
    }

    private LocationHistory(String fileName) {
        super(HISTORY_DIR);
        ensureFileAndDirectoryExist(fileName);

        registerField("locations", this::getLocations, v -> {
            locations.clear();
            locations.putAll((Map<Long, LocationDto>) v);
        }, new TypeToken<Map<String, LocationDto>>() {
        }.getType());

        loadFromDisk();
    }
    /// ---------------------------------------------------------------------------------


    Map<Long, LocationDto> locations = new HashMap<>();

    public void addLocations(Map<Long, LocationDto> locations) {
        this.locations.putAll(locations);
        save();
    }

    public Map<Long, LocationDto> getLocations() {
        return locations;
    }


    /// ---------------------------------------------------------------------------------
    private void loadFromDisk() {
        loadSession(LocationHistory.this::loadFields);
    }



    public List<String> listStarSystems() {
        File file = new File(super.APP_DIR, super.sessionFile);
        File parentDir = file.getParentFile();
        File[] files = parentDir.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        if (files != null) {
            for(File f : files) {
                fileNames.add(f.getName().replace(".json", ""));
            }
        }
        return fileNames;
    }
}