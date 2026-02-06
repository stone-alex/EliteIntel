package elite.intel.gameapi.journal.events.dto;

public class GenusDto {

    private String planetName;
    private String species;
    private String variant;
    private long rewardInCredits;
    private long bonusCreditsForFirstDiscovery;


    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getVarient() {
        return variant;
    }

    public void setVarient(String varient) {
        this.variant = varient;
    }

    public long getRewardInCredits() {
        return rewardInCredits;
    }

    public void setRewardInCredits(long rewardInCredits) {
        this.rewardInCredits = rewardInCredits;
    }

    public long getBonusCreditsForFirstDiscovery() {
        return bonusCreditsForFirstDiscovery;
    }

    public void setBonusCreditsForFirstDiscovery(long bonusCreditsForFirstDiscovery) {
        this.bonusCreditsForFirstDiscovery = bonusCreditsForFirstDiscovery;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }
}
