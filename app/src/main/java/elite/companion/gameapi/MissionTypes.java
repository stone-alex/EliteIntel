package elite.companion.gameapi;

public enum MissionTypes {

    PIRATES("Pirates");

    private final String missionType;

    MissionTypes(String missionType) {
        this.missionType = missionType;
    }

    public String getMissionType() {
        return this.missionType;
    }

}
