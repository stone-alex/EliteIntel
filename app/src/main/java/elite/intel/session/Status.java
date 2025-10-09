package elite.intel.session;

import elite.intel.gameapi.gamestate.dtos.GameEvents;

public class Status extends StatusFlags implements java.io.Serializable {

    private static final String SESSION_DIR = "session/";
    private static volatile Status instance; // Singleton instance
    private GameEvents.StatusEvent gameStatus = new GameEvents.StatusEvent();
    private Long lastStatusChange = null;

    public static Status getInstance() {
        if (instance == null) {
            synchronized (Status.class) {
                if (instance == null) {
                    instance = new Status("status.json");
                }
            }
        }
        return instance;
    }

    private Status(String fileName) {
        super(SESSION_DIR);
        ensureFileAndDirectoryExist(fileName);
        loadFromDisk();
        registerField("game_status", this::getStatus, this::setStatus, GameEvents.StatusEvent.class);
        registerField("last_change", this::getLastStatusChange, this::setLastStatusChange, Long.class);
    }

    public GameEvents.StatusEvent getStatus() {
        return this.gameStatus;
    }

    public void setStatus(GameEvents.StatusEvent event) {
        this.gameStatus = event;
        lastStatusChange = System.currentTimeMillis();
        save();
    }

    public Long getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(Long lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    public void loadFromDisk() {
        loadSession(Status.this::loadFields);
    }


    public boolean isInSrv() {
        return isInSrv(getStatus().getFlags());
    }

    public boolean isInMainShip() {
        return isInMainShip(getStatus().getFlags());
    }

    public boolean isInFighter() {
        return isInFighter(getStatus().getFlags());
    }

    public boolean isInWing() {
        return isInWing(getStatus().getFlags());
    }

    public boolean isHardpointsDeployed() {
        return isHardpointsDeployed(getStatus().getFlags());
    }

    public boolean isCargoScoopDeployed() {
        return isCargoScoopDeployed(getStatus().getFlags());
    }

    public boolean isLightsOn() {
        return isLightsOn(getStatus().getFlags());
    }

    public boolean isSilentRunning() {
        return isSilentRunning(getStatus().getFlags());
    }

    public boolean isScoopingFuel() {
        return isScoopingFuel(getStatus().getFlags());
    }

    public boolean isSrvHandbrake() {
        return isSrvHandbrake(getStatus().getFlags());
    }

    public boolean isOnFoot() {
        return isOnFoot(getStatus().getFlags());
    }

    public boolean isAimDownSight() {
        return isAimingDownSights(getStatus().getFlags());
    }

    public boolean isLowOxygen() {
        return isLowOxygenF2(getStatus().getFlags2());
    }

    public boolean isLowHealth() {
        return isLowHealthF2(getStatus().getFlags2());
    }

    public boolean isHot() {
        return isHot(getStatus().getFlags());
    }

    public boolean isCold() {
        return isCold(getStatus().getFlags());
    }

    public boolean isNightVision() {
        return isNightVision(getStatus().getFlags());
    }

    public boolean isAnalysisMode() {
        return isHudAnalysisMode(getStatus().getFlags());
    }

    public boolean isFsdJump() {
        return isFsdJump(getStatus().getFlags());
    }

    public boolean isSrvHighBeam() {
        return isSrvHighBeam(getStatus().getFlags());
    }

    public boolean isFsdCharging() {
        return isFsdCharging(getStatus().getFlags());
    }

    public boolean isFsdCooldown() {
        return isFsdCooldown(getStatus().getFlags());
    }

    public boolean isFsdMassLocked() {
        return isFsdMassLocked(getStatus().getFlags());
    }

    public boolean isInTaxi() {
        return isInTaxi(getStatus().getFlags2());
    }

    public boolean isInMulticrew() {
        return isInMulticrew(getStatus().getFlags2());
    }

    public boolean isOnFootInStation() {
        return isOnFootInStation(getStatus().getFlags2());
    }

    public boolean isOnFootOnPlanet() {
        return isOnFootOnPlanet(getStatus().getFlags2());
    }

    public boolean isOnFootInHangar() {
        return isOnFootInHangar(getStatus().getFlags2());
    }

    public boolean isOnFootSocialSpace() {
        return isOnFootSocialSpace(getStatus().getFlags2());
    }

    public boolean isOnFootExterior() {
        return isOnFootExterior(getStatus().getFlags2());
    }

    public boolean isBreathableAtmosphere() {
        return isBreathableAtmosphere(getStatus().getFlags2());
    }

    public boolean isGlideMode() {
        return isGlideModeF2(getStatus().getFlags2());
    }

    public boolean isVeryHot() {
        return isVeryHot(getStatus().getFlags2());
    }

    public boolean isVeryCold() {
        return isVeryCold(getStatus().getFlags2());
    }

    public boolean isTelepresenceMulticrew() {
        return isTelepresenceMulticrew(getStatus().getFlags2());
    }

    public boolean isPhysicalMulticrew() {
        return isPhysicalMulticrew(getStatus().getFlags2());
    }

    public boolean isFsdHyperdriveCharging() {
        return isFsdHyperdriveCharging(getStatus().getFlags2());
    }

    public boolean isAltFromAvgRadius() {
        return isAltFromAvgRadius(getStatus().getFlags2());
    }

    public boolean isDocked() {
        return isDocked(getStatus().getFlags());
    }

    public boolean isLanded() {
        return isLanded(getStatus().getFlags());
    }

    public boolean isLandingGearDown() {
        return isLandingGearDown(getStatus().getFlags());
    }

    public boolean isShieldsUp() {
        return isShieldsUp(getStatus().getFlags2());
    }

    public boolean isInSupercruise() {
        return isInSupercruise(getStatus().getFlags());
    }

    public boolean isFlightAssistOff() {
        return isFlightAssistOff(getStatus().getFlags2());
    }

    public boolean isSrvTurretView() {
        return isSrvTurretView(getStatus().getFlags2());
    }

    public boolean isSrvTurretRetracted() {
        return isSrvTurretRetracted(getStatus().getFlags2());
    }

    public boolean isSrvDriveAssist() {
        return isSrvDriveAssist(getStatus().getFlags2());
    }

    public boolean isLowFuel() {
        return isLowFuel(getStatus().getFlags());
    }

    public boolean isOverheating() {
        return isOverheating(getStatus().getFlags());
    }

    public boolean hasLatLong() {
        return hasLatLong(getStatus().getFlags());
    }

    public boolean isInDanger() {
        return isInDanger(getStatus().getFlags());
    }

    public boolean isBeingInterdicted() {
        return isBeingInterdicted(getStatus().getFlags());
    }
}

