package elite.intel.about.dto;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;


public record AICapabilitiesDto(String supportedCommands, String customCommands, String supportedQueries, String description) implements ToJsonConvertible {

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

}
