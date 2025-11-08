package elite.intel.gameapi.data;

import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class FsdTarget implements ToJsonConvertible {
    private StarSystemDto systemDto;
    private DeathsDto deathsDto;
    private TrafficDto trafficDto;
    private String fuelStarStatus;

    public FsdTarget(StarSystemDto systemDto, DeathsDto deathsDto, TrafficDto trafficDto, String fuelStarStatus) {
        this.systemDto = systemDto;
        this.deathsDto = deathsDto;
        this.trafficDto = trafficDto;
        this.fuelStarStatus = fuelStarStatus;
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
