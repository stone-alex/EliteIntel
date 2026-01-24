package elite.intel.help.dto;

import elite.intel.help.EliteIntelFactory;
import elite.intel.util.ExoBio;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;


public record AICapabilitiesDto(EliteIntelFactory.DataDto dataDto, String description) implements ToJsonConvertible {

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

}
