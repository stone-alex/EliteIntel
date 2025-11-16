package elite.intel.session;

import elite.intel.ai.search.spansh.station.traderandbroker.TraderAndBrokerSearchDto;

public class DestinationReminder extends SessionPersistence implements java.io.Serializable {

    private static final String DIRECTORY = "session/";
    private static final String DESTINATION = "destination";
    private static volatile DestinationReminder instance;
    private TraderAndBrokerSearchDto.Result destination;

    private DestinationReminder(String directory) {
        super(directory);
        ensureFileAndDirectoryExist("destination_reminder.json");
        registerField(DESTINATION, this::getDestination, this::setDestination, TraderAndBrokerSearchDto.Result.class);
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

    public TraderAndBrokerSearchDto.Result getDestination() {
        return destination;
    }

    public void setDestination(TraderAndBrokerSearchDto.Result destination) {
        this.destination = destination;
        save();
    }

    private void loadFromDisk() {
        loadSession(DestinationReminder.this::loadFields);
    }
}
