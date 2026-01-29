package elite.intel.gameapi;

public enum MissionType {
    // Fighting
    MISSION_PIRATE_MASSACRE("Mission_Massacre"),
    MISSION_PIRATE_MASSACRE_WING("Mission_MassacreWing"),
    MISSION_ASSASSINATE_PLANETARY_EXPANSION("Mission_Assassinate_Planetary_Expansion"),
    MISSION_PIRACY_ANARCHY("Mission_Piracy_Anarchy"),

    // Ranking missions
    MISSION_ASSASSINATE_RANKEMP("Mission_Assassinate_RankEmp"),
    MISSION_ASSASSINATE_RANKFED("Mission_Assassinate_RankFed"), // Presumed name convention for Federation

    // Courier
    MISSION_COURIER("Mission_Courier"),
    MISSION_COURIER_ELECTIONS("Mission_Courier_Elections"),
    MISSION_COURIER_EXPANSION("Mission_Courier_Expansion"),
    MISSION_COURIER_OUTBREAK("Mission_Courier_Outbreak"),
    MISSION_COURIER_SERVICE("Mission_Courier_Service"),
    MISSION_DELIVERY("Mission_Delivery"),
    MISSION_COURIER_CIVILWAR("Mission_Courier_CivilWar"),

    //  Passenger
    MISSION_PASSENGERBULK("Mission_PassengerBulk"),
    MISSION_PASSENGERVIP("Mission_PassengerVIP"),
    MISSION_SIGHTSEEING("Mission_Sightseeing"),
    MISSION_RESCUE_ELECTIONS("Mission_Rescue_Elections"),
    MISSION_LONGDISTANCEEXPEDITION_EXPLORER_BOOM("Mission_LongDistanceExpedition_Explorer_Boom"),
    MISSION_PASSENGERBULK_AIDWORKER_ARRIVING("Mission_PassengerBulk_AIDWORKER_ARRIVING"),
    MISSION_PASSENGERVIP_TERRORIST_CIVILWAR("Mission_PassengerVIP_Terrorist_CIVILWAR"),
    MISSION_SIGHTSEEING_CRIMINAL_CIVILWAR("Mission_Sightseeing_Criminal_CIVILWAR"),
    MISSION_SIGHTSEEING_SCIENTIST_CIVILWAR("Mission_Sightseeing_Scientist_CIVILWAR"),
    MISSION_SIGHTSEEING_TERRORIST_CIVILWAR("Mission_Sightseeing_Terrorist_CIVILWAR"),
    MISSION_SIGHTSEEING_TOURIST_BOOM("Mission_Sightseeing_Tourist_BOOM"),
    MISSION_SIGHTSEEING_WHISTLEBLOWER_ELECTION("Mission_Sightseeing_Whistleblower_ELECTION"),
    MISSION_PASSENGERBULK_PRISONEROFWAR_LEAVING("Mission_PassengerBulk_PRISONEROFWAR_LEAVING"),

    // Alturism
    MISSION_ALTRUISM("Mission_Altruism"),
    MISSION_ALTRUISMCREDITS("Mission_AltruismCredits"),
    MISSION_ALTRUISMCREDITS_OUTBREAK("Mission_AltruismCredits_Outbreak"),

    // Salvage
    MISSION_SALVAGE_ILLEGAL("Mission_Salvage_Illegal"),
    MISSION_RESCUE_PLANET_CIVILWAR("Mission_Rescue_Planet_CivilWar"),

    // Collecting
    MISSION_COLLECT_INDUSTRIAL("Mission_Collect_Industrial"),

    // Mining
    MISSION_MINING("Mission_Mining"),

    // OnFoot / Oddesy
    MISSION_ONFOOT_ONSLAUGHT_OFFLINE_002("Mission_OnFoot_Onslaught_Offline_002"),
    MISSION_ONFOOT_SALVAGE_MB("Mission_OnFoot_Salvage_MB"),
    MISSION_ONFOOT_SALVAGEILLEGAL_MB("Mission_OnFoot_SalvageIllegal_MB"),
    MISSION_HACK_BLOPS_BOOM("Mission_Hack_BLOPS_Boom"),
    MISSION_ONFOOT_ONSLAUGHTILLEGAL("Mission_OnFoot_OnslaughtIllegal"),
    MISSION_ONFOOT_HEIST_POI("Mission_OnFoot_Heist_POI"),
    MISSION_ONFOOT_ASSASSINATION_HARD("Mission_OnFoot_Assassination_Hard"),
    MISSION_ONFOOT_ASSASSINATION_COVERT("Mission_OnFoot_Assassination_Covert"),
    MISSION_ONFOOT_MASSACREILLEGAL("Mission_OnFoot_MassacreIllegal"),
    MISSION_ONFOOT_ASSASSINATION("Mission_OnFoot_Assassination"),
    MISSION_ONFOOT_DELIVERY_CONTACT("Mission_OnFoot_Delivery_Contact"),
    MISSION_ONFOOT_ONSLAUGHT("Mission_OnFoot_Onslaught"),
    // todo: verify ranking mission
    MISSION_COLLECT_RANKEMP("Mission_Collect_RankEmp"),
    MISSION_RESCUE_PLANET("Mission_Rescue_Planet"),
    MISSION_SCAN("Mission_Scan"),
    MISSION_ASSASSINATE_ILLEGAL_BLOPS_CIVILLIBERTY("Mission_assassinate_Illegal_BLOPS_CivilLiberty"),
    MISSION_ASSASSINATE_PLANETARY("Mission_Assassinate_Planetary"),
    MISSION_ONFOOT_ASSASSINATION_MB("Mission_OnFoot_Assassination_MB"),
    MISSION_ONFOOT_COLLECT_MB("Mission_OnFoot_Collect_MB"),

    UNKNOWN("UnknownType");

    private final String missionType;

    MissionType(String missionType) {
        this.missionType = missionType;
    }

    public String getMissionType() {
        return this.missionType;
    }

    public static MissionType getUnknown() {
        return UNKNOWN;
    }

}
