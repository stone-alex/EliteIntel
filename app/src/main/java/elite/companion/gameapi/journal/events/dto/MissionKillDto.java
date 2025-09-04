package elite.companion.gameapi.journal.events.dto;

import elite.companion.gameapi.gamestate.events.BaseJsonDto;

public class MissionKillDto extends BaseJsonDto {

    private String targetFaction;
    private String victimPilotName;

    public String getTargetFaction() {
        return targetFaction;
    }

    public void setTargetFaction(String targetFaction) {
        this.targetFaction = targetFaction;
    }

    public String getVictimPilotName() {
        return victimPilotName;
    }

    public void setVictimPilotName(String victimPilotName) {
        this.victimPilotName = victimPilotName;
    }
}
