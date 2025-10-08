package elite.intel.gameapi.journal.events.dto;

public class GenusDto {

    private String species;
    private String varient;
    private long rewardInCredits;
    private long bonusForFirstDiscovery;


    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getVarient() {
        return varient;
    }

    public void setVarient(String varient) {
        this.varient = varient;
    }

    public long getRewardInCredits() {
        return rewardInCredits;
    }

    public void setRewardInCredits(long rewardInCredits) {
        this.rewardInCredits = rewardInCredits;
    }

    public long getBonusForFirstDiscovery() {
        return bonusForFirstDiscovery;
    }

    public void setBonusForFirstDiscovery(long bonusForFirstDiscovery) {
        this.bonusForFirstDiscovery = bonusForFirstDiscovery;
    }
}
