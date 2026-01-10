package elite.intel.gameapi;

public enum MissionTypes {

    PIRATES("Pirates"), DELIVERY("Delivery"), PASSENGER("Passenger");

    private final String missionType;

    MissionTypes(String missionType) {
        this.missionType = missionType;
    }

    public String getMissionType() {
        return this.missionType;
    }

}
