package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class BountyDto extends BaseJsonDto implements ToJsonConvertible {

    private List<BountyDto.Reward> rewards;
    private String pilotName;
    private String pilotNameLocalised;
    private String target;
    private long totalReward;
    private String victimFaction;

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public String getPilotName() {
        return pilotName;
    }

    public void setPilotName(String pilotName) {
        this.pilotName = pilotName;
    }

    public String getPilotNameLocalised() {
        return pilotNameLocalised;
    }

    public void setPilotNameLocalised(String pilotNameLocalised) {
        this.pilotNameLocalised = pilotNameLocalised;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(long totalReward) {
        this.totalReward = totalReward;
    }

    public String getVictimFaction() {
        return victimFaction;
    }

    public void setVictimFaction(String victimFaction) {
        this.victimFaction = victimFaction;
    }

    public static class Reward {
        private String faction;
        private long reward;

        public String getFaction() {
            return faction;
        }

        public void setFaction(String faction) {
            this.faction = faction;
        }

        public long getReward() {
            return reward;
        }

        public void setReward(long reward) {
            this.reward = reward;
        }
    }
}
