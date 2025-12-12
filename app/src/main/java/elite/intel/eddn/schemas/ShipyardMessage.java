package elite.intel.eddn.schemas;


import java.util.List;

public class ShipyardMessage {
    public String timestamp;
    public String systemName;
    public String stationName;
    public long marketId;
    public boolean horizons = true;
    public boolean odyssey = true;
    public List<String> ships;

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setMarketId(long marketId) {
        this.marketId = marketId;
    }

    public void setHorizons(boolean horizons) {
        this.horizons = horizons;
    }

    public void setOdyssey(boolean odyssey) {
        this.odyssey = odyssey;
    }

    public void setShips(List<String> ships) {
        this.ships = ships;
    }
}
