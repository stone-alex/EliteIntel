package elite.companion.gameapi.journal.events.dto;

import com.google.gson.Gson;
import elite.companion.gameapi.journal.events.PlayerBasicStats;
import elite.companion.util.Ranks;

public class RankDto {

    private String combatRank = "unknown";
    private String militaryRankEmpire = "unknown";
    private String militaryRankFederation = "unknown";
    private String highestMilitaryRank = "unknown";
    private String honorific = "Commander";
    private String exobiologyRank = "unknown";
    private String explorationRank = "unknown";
    private String mercenaryRank = "unknown";

    public String getCombatRank() {
        return combatRank;
    }

    public void setCombatRank(String combatRank) {
        this.combatRank = combatRank;
    }

    public String getMilitaryRankEmpire() {
        return militaryRankEmpire;
    }

    public void setMilitaryRankEmpire(String militaryRankEmpire) {
        this.militaryRankEmpire = militaryRankEmpire;
    }

    public String getMilitaryRankFederation() {
        return militaryRankFederation;
    }

    public void setMilitaryRankFederation(String militaryRankFederation) {
        this.militaryRankFederation = militaryRankFederation;
    }

    public String getHighestMilitaryRank() {
        return highestMilitaryRank;
    }

    public void setHighestMilitaryRank(String highestMilitaryRank) {
        this.highestMilitaryRank = highestMilitaryRank;
    }

    public String getHonorific() {
        return honorific == null ? "Commander" : honorific;
    }

    public void setHonorific(String honorific) {
        this.honorific = honorific;
    }

    public String getExobiologyRank() {
        return exobiologyRank;
    }

    public void setExobiologyRank(String exobiologyRank) {
        this.exobiologyRank = exobiologyRank;
    }

    public String getExplorationRank() {
        return explorationRank;
    }

    public void setExplorationRank(String explorationRank) {
        this.explorationRank = explorationRank;
    }

    public String getMercenaryRank() {
        return mercenaryRank;
    }

    public void setMercenaryRank(String mercenaryRank) {
        this.mercenaryRank = mercenaryRank;
    }


    public String toJson() {
        return new Gson().toJson(this);
    }

    public void setData(PlayerBasicStats data) {
        this.setCombatRank(Ranks.getCombatRankMap().get(data.getCombat()));
        this.setExobiologyRank(Ranks.getExobiologyRankMap().get(data.getCombat()));
        this.setExplorationRank(Ranks.getExplorationRankMap().get(data.getCombat()));
        this.setHighestMilitaryRank(Ranks.getHighestRankAsString(data.getEmpire(), data.getFederation()));
        this.setMilitaryRankEmpire(Ranks.getImperialRankMap().get(data.getEmpire()));
        this.setMilitaryRankFederation(Ranks.getFederationRankMap().get(data.getFederation()));
        this.setMercenaryRank(Ranks.getMercenaryRankMap().get(data.getSoldier()));
        this.setHonorific(Ranks.getHonorific(data.getEmpire(), data.getFederation()));
    }

}
