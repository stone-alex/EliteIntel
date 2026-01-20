package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class ShipLoadOutDto extends BaseJsonDto implements ToJsonConvertible {


    private String shipMake;
    private String shipName;
    private int shipId;
    private String shipIdent;
    private long modulesValue;
    private float hullHealth;
    private float unladenMass;
    private int cargoCapacity;
    private double maxJumpRange;
    private long insurance;

    private FuelCapacityDto fuelCapacity;
    private List<ModuleDto> modules;

    public String getShipMake() {
        return shipMake;
    }

    public void setShipMake(String shipMake) {
        this.shipMake = shipMake;
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getShipIdent() {
        return shipIdent;
    }

    public void setShipIdent(String shipIdent) {
        this.shipIdent = shipIdent;
    }

    public long getModulesValue() {
        return modulesValue;
    }

    public void setModulesValue(long modulesValue) {
        this.modulesValue = modulesValue;
    }

    public float getHullHealth() {
        return hullHealth;
    }

    public void setHullHealth(float hullHealth) {
        this.hullHealth = hullHealth;
    }

    public float getUnladenMass() {
        return unladenMass;
    }

    public void setUnladenMass(float unladenMass) {
        this.unladenMass = unladenMass;
    }

    public int getCargoCapacity() {
        return cargoCapacity;
    }

    public void setCargoCapacity(int cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    public double getMaxJumpRange() {
        return maxJumpRange;
    }

    public void setMaxJumpRange(double maxJumpRange) {
        this.maxJumpRange = maxJumpRange;
    }

    public long getInsurance() {
        return insurance;
    }

    public void setInsurance(long insurance) {
        this.insurance = insurance;
    }

    public FuelCapacityDto getFuelCapacity() {
        return fuelCapacity;
    }

    public void setFuelCapacity(FuelCapacityDto fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public List<ModuleDto> getModules() {
        return modules;
    }

    public void setModules(List<ModuleDto> modules) {
        this.modules = modules;
    }
}
