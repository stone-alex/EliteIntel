package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class EngineeringDto extends BaseJsonDto implements ToJsonConvertible {

    private String engineer;
    //private Integer engineerId;
    //private Integer blueprintId;
    private String blueprintName;
    //private Integer level;
    private Float quality;
    //private String experimentalEffect;
    private String experimentalEffectLocalised;
    private List<ModifierDto> modifiers;

    public String getEngineer() {
        return engineer;
    }

    public void setEngineer(String engineer) {
        this.engineer = engineer;
    }

/*
    public Integer getEngineerId() {
        return engineerId;
    }

    public void setEngineerId(Integer engineerId) {
        this.engineerId = engineerId;
    }

    public Integer getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(Integer blueprintId) {
        this.blueprintId = blueprintId;
    }
*/

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public Float getQuality() {
        return quality;
    }

    public void setQuality(Float quality) {
        this.quality = quality;
    }

    public String getExperimentalEffectLocalised() {
        return experimentalEffectLocalised;
    }


    public void setExperimentalEffectLocalised(String experimentalEffectLocalised) {
        this.experimentalEffectLocalised = experimentalEffectLocalised;
    }

    public List<ModifierDto> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierDto> modifiers) {
        this.modifiers = modifiers;
    }
}
