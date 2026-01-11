package elite.intel.gameapi;

public enum MissionTypes {
    // Fighting
    PIRATES("Pirates"),
    MISSION_ASSASSINATE_PLANETARY_EXPANSION("Mission_Assassinate_Planetary_Expansion"),
    MISSION_ASSASSINATE_PLANETARY_EXPANSION_NAME("Mission_Assassinate_Planetary_Expansion_name"),
    MISSION_ASSASSINATE_RANKEMP("Mission_Assassinate_RankEmp"),
    MISSION_ASSASSINATE_RANKEMP_NAME("Mission_Assassinate_RankEmp_name"),
    MISSION_MASSACREWING("Mission_MassacreWing"),
    MISSION_MASSACREWING_NAME("Mission_MassacreWing_name"),
    MISSION_PIRACY_ANARCHY("Mission_Piracy_Anarchy"),

    // Courier
    MISSION_COURIER("Mission_Courier"),
    MISSION_COURIER_ELECTIONS("Mission_Courier_Elections"),
    MISSION_COURIER_ELECTIONS_NAME("Mission_Courier_Elections_name"),
    MISSION_COURIER_EXPANSION("Mission_Courier_Expansion"),
    MISSION_COURIER_EXPANSION_NAME("Mission_Courier_Expansion_name"),
    MISSION_COURIER_NAME("Mission_Courier_name"),
    MISSION_COURIER_OUTBREAK("Mission_Courier_Outbreak"),
    MISSION_COURIER_OUTBREAK_NAME("Mission_Courier_Outbreak_name"),
    MISSION_COURIER_SERVICE("Mission_Courier_Service"),
    MISSION_COURIER_SERVICE_NAME("Mission_Courier_Service_name"),
    MISSION_DELIVERY("Mission_Delivery"),

    //  Passenger
    //PASSENGER("Passenger"),

    // Rescue
    MISSION_RESCUE_ELECTIONS("Mission_Rescue_Elections"),
    MISSION_RESCUE_ELECTIONS_NAME("Mission_Rescue_Elections_name"),

    // Alturism
    MISSION_ALTRUISM("Mission_Altruism"),
    MISSION_ALTRUISMCREDITS_OUTBREAK("Mission_AltruismCredits_Outbreak"),
    MISSION_ALTRUISMCREDITS("Mission_AltruismCredits"),
    MISSION_ALTRUISMCREDITS_NAME("Mission_AltruismCredits_name"),
    MISSION_ALTRUISMCREDITS_OUTBREAK_NAME("Mission_AltruismCredits_Outbreak_name"),

    // Salvage
    MISSION_SALVAGE_ILLEGAL("MISSION_Salvage_Illegal"),

    // Collecting
    MISSION_COLLECT_INDUSTRIAL("Mission_Collect_Industrial"),

    // OnFoot,
    MISSION_ONFOOT_ONSLAUGHT_OFFLINE_002("Mission_OnFoot_Onslaught_Offline_002"),
    MISSION_ONFOOT_ONSLAUGHT_OFFLINE_002_NAME("Mission_OnFoot_Onslaught_Offline_002_name");

    private final String missionType;

    MissionTypes(String missionType) {
        this.missionType = missionType;
    }

    public String getMissionType() {
        return this.missionType;
    }

}
