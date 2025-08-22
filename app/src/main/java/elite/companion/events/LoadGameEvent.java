package elite.companion.events;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

public class LoadGameEvent extends BaseEventEvent {
    private String FID;
    private String Commander;
    private boolean Horizons;
    private boolean Odyssey;
    private String Ship;
    private int ShipID;
    private String ShipName;
    private String ShipIdent;
    private double FuelLevel;
    private double FuelCapacity;
    private String GameMode;
    private long Credits;
    private long Loan;
    private String language;
    private String gameversion;
    private String build;

    public LoadGameEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), LoadGameEvent.class.getName());
    }

    public String getFID() {
        return FID;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public String getCommander() {
        return Commander;
    }

    public void setCommander(String commander) {
        Commander = commander;
    }

    public boolean isHorizons() {
        return Horizons;
    }

    public void setHorizons(boolean horizons) {
        Horizons = horizons;
    }

    public boolean isOdyssey() {
        return Odyssey;
    }

    public void setOdyssey(boolean odyssey) {
        Odyssey = odyssey;
    }

    public String getShip() {
        return Ship;
    }

    public void setShip(String ship) {
        Ship = ship;
    }

    public int getShipID() {
        return ShipID;
    }

    public void setShipID(int shipID) {
        ShipID = shipID;
    }

    public String getShipName() {
        return ShipName;
    }

    public void setShipName(String shipName) {
        ShipName = shipName;
    }

    public String getShipIdent() {
        return ShipIdent;
    }

    public void setShipIdent(String shipIdent) {
        ShipIdent = shipIdent;
    }

    public double getFuelLevel() {
        return FuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        FuelLevel = fuelLevel;
    }

    public double getFuelCapacity() {
        return FuelCapacity;
    }

    public void setFuelCapacity(double fuelCapacity) {
        FuelCapacity = fuelCapacity;
    }

    public String getGameMode() {
        return GameMode;
    }

    public void setGameMode(String gameMode) {
        GameMode = gameMode;
    }

    public long getCredits() {
        return Credits;
    }

    public void setCredits(long credits) {
        Credits = credits;
    }

    public long getLoan() {
        return Loan;
    }

    public void setLoan(long loan) {
        Loan = loan;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGameversion() {
        return gameversion;
    }

    public void setGameversion(String gameversion) {
        this.gameversion = gameversion;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadGameEvent that = (LoadGameEvent) o;
        return Horizons == that.Horizons &&
                Odyssey == that.Odyssey &&
                ShipID == that.ShipID &&
                Double.compare(that.FuelLevel, FuelLevel) == 0 &&
                Double.compare(that.FuelCapacity, FuelCapacity) == 0 &&
                Credits == that.Credits &&
                Loan == that.Loan &&
                Objects.equals(FID, that.FID) &&
                Objects.equals(Commander, that.Commander) &&
                Objects.equals(Ship, that.Ship) &&
                Objects.equals(ShipName, that.ShipName) &&
                Objects.equals(ShipIdent, that.ShipIdent) &&
                Objects.equals(GameMode, that.GameMode) &&
                Objects.equals(language, that.language) &&
                Objects.equals(gameversion, that.gameversion) &&
                Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FID, Commander, Horizons, Odyssey, Ship, ShipID, ShipName, ShipIdent,
                FuelLevel, FuelCapacity, GameMode, Credits, Loan, language, gameversion, build);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LoadGameEvent.class.getSimpleName() + "[", "]")
                .add("FID='" + FID + "'")
                .add("Commander='" + Commander + "'")
                .add("Horizons=" + Horizons)
                .add("Odyssey=" + Odyssey)
                .add("Ship='" + Ship + "'")
                .add("ShipID=" + ShipID)
                .add("ShipName='" + ShipName + "'")
                .add("ShipIdent='" + ShipIdent + "'")
                .add("FuelLevel=" + FuelLevel)
                .add("FuelCapacity=" + FuelCapacity)
                .add("GameMode='" + GameMode + "'")
                .add("Credits=" + Credits)
                .add("Loan=" + Loan)
                .add("language='" + language + "'")
                .add("gameversion='" + gameversion + "'")
                .add("build='" + build + "'")
                .toString();
    }

}
