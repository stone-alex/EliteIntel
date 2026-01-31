package elite.intel.session;

public class LocationData<A, I> {

    private final A systemAddress;
    private final I inGameId;

    public LocationData(A systemAddress, I idGameID) {
        this.systemAddress = systemAddress;
        this.inGameId = idGameID;
    }

    public A getSystemAddress() {
        return systemAddress;
    }

    public I getInGameId() {
        return inGameId;
    }
}
