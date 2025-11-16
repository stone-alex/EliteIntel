package elite.intel.session;

import elite.intel.ai.search.spansh.station.DestinationDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class DestinationReminder extends SessionPersistence implements java.io.Serializable {

    private static final String DIRECTORY = "session/";
    private static final String DESTINATION = "destination";
    private static volatile DestinationReminder instance;
    private DestinationDto destination;

    private DestinationReminder(String directory) {
        super(directory);
        ensureFileAndDirectoryExist("destination_reminder.json");
        registerField(DESTINATION, this::getDestination, this::setDestination, DestinationDto.class);
        loadFromDisk();
    }

    public static DestinationReminder getInstance() {
        if (instance == null) {
            synchronized (DestinationReminder.class) {
                if (instance == null) {
                    instance = new DestinationReminder(DIRECTORY);
                }
            }
        }
        return instance;
    }

    public DestinationDto getDestination() {
        return destination;
    }

    public void setDestination(DestinationDto destination) {
        this.destination = destination;
    }

    public void setDestinationFromJson(String destinationAsJson) {
        DestinationDto destinationDto = GsonFactory.getGson().fromJson(destinationAsJson, DestinationDto.class);
        this.destination = destinationDto;
        save();
    }

    private void loadFromDisk() {
        loadSession(DestinationReminder.this::loadFields);
    }
}
