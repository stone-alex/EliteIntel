package elite.intel.gameapi.data;

import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.StarSystemDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class FsdTarget implements ToJsonConvertible {
    private LocationDto location;
    private StarSystemDto systemDto;
    private DeathsDto deathsDto;
    private TrafficDto trafficDto;
    private String fuelStarStatus;

    public FsdTarget(LocationDto location, StarSystemDto systemDto, DeathsDto deathsDto, TrafficDto trafficDto, String fuelStarStatus) {
        this.location = location;
        this.systemDto = systemDto;
        this.deathsDto = deathsDto;
        this.trafficDto = trafficDto;
        this.fuelStarStatus = fuelStarStatus;
    }

    public LocationDto getLocation() {
        return location;
    }

    public StarSystemDto getSystemDto() {
        return systemDto;
    }

    public DeathsDto getDeathsDto() {
        return deathsDto;
    }

    public TrafficDto getTrafficDto() {
        return trafficDto;
    }

    public String getFuelStarStatus() {
        return fuelStarStatus;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
