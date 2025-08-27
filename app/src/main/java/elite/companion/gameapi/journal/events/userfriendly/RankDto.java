package elite.companion.gameapi.journal.events.userfriendly;

import com.google.gson.Gson;

public class RankDto {

    private String combatRank;
    private String militaryRankEmpire;
    private String militaryRankFederation;
    private String highestMilitaryRank;
    private String honorific;
    private String exobiologyRank;
    private String explorationRank;
    private String mercenaryRank;

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
        return honorific;
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
}
