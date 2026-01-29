package elite.intel.gameapi;

public enum MissionTargets {

    KNOWN_TERRORIST("Known Terrorist"),
    KNOWN_PIRATE("Known Pirate"),
    PIRATES("Pirates"),
    UNKNOWN("Unknown");

    private final String targetType;

    MissionTargets(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetType() {
        return this.targetType;
    }

    public static MissionTargets getUnknown() {
        return UNKNOWN;
    }
}
