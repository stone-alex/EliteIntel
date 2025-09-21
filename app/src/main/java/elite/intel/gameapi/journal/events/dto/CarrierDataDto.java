package elite.intel.gameapi.journal.events.dto;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class CarrierDataDto implements ToJsonConvertible {


    private String location;
    private long totalBalance;
    private long reserveBalance;
    private String callSign;
    private String carrierName;
    private String carrierType;
    private String dockingAccess;
    private double currentJumpRange;
    private double maxJumpRange;
    private boolean allowNotorious;
    private boolean isPendingDecommission;
    private String spaceUsage;
    private String finance;
    private String crew;
    private int cargoSpaceUsed;
    private int cargoSpaceReserved;
    private int shipRacks;
    private int modulePacks;
    private int freeSpaceInCargo;
    private int cargoCapacity;
    private long marketBalance;
    private int pioneerSupplyTax;
    private int shipYardSupplyTax;
    private int rearmSupplyTax;
    private int refuelSupplyTax;
    private int repairSupplyTax;
    private int fuelSupply=0;
    private double x,y,z;

    public long getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(long totalBalance) {
        this.totalBalance = totalBalance;
    }

    public long getReserveBalance() {
        return reserveBalance;
    }

    public void setReserveBalance(long reserveBalance) {
        this.reserveBalance = reserveBalance;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getCarrierType() {
        return carrierType;
    }

    public void setCarrierType(String carrierType) {
        this.carrierType = carrierType;
    }

    public String getDockingAccess() {
        return dockingAccess;
    }

    public void setDockingAccess(String dockingAccess) {
        this.dockingAccess = dockingAccess;
    }

    public double getCurrentJumpRange() {
        return currentJumpRange;
    }

    public void setCurrentJumpRange(double currentJumpRange) {
        this.currentJumpRange = currentJumpRange;
    }

    public double getMaxJumpRange() {
        return maxJumpRange;
    }

    public void setMaxJumpRange(double maxJumpRange) {
        this.maxJumpRange = maxJumpRange;
    }

    public boolean isAllowNotorious() {
        return allowNotorious;
    }

    public void setAllowNotorious(boolean allowNotorious) {
        this.allowNotorious = allowNotorious;
    }

    public boolean isPendingDecommission() {
        return isPendingDecommission;
    }

    public void setPendingDecommission(boolean pendingDecommission) {
        isPendingDecommission = pendingDecommission;
    }

    public String getSpaceUsage() {
        return spaceUsage;
    }

    public void setSpaceUsage(String spaceUsage) {
        this.spaceUsage = spaceUsage;
    }

    public String getFinance() {
        return finance;
    }

    public void setFinance(String finance) {
        this.finance = finance;
    }

    public String getCrew() {
        return crew;
    }

    public void setCrew(String crew) {
        this.crew = crew;
    }

    public int getCargoSpaceUsed() {
        return cargoSpaceUsed;
    }

    public void setCargoSpaceUsed(int cargoSpaceUsed) {
        this.cargoSpaceUsed = cargoSpaceUsed;
    }

    public int getCargoSpaceReserved() {
        return cargoSpaceReserved;
    }

    public void setCargoSpaceReserved(int cargoSpaceReserved) {
        this.cargoSpaceReserved = cargoSpaceReserved;
    }

    public int getShipRacks() {
        return shipRacks;
    }

    public void setShipRacks(int shipRacks) {
        this.shipRacks = shipRacks;
    }

    public int getModulePacks() {
        return modulePacks;
    }

    public void setModulePacks(int modulePacks) {
        this.modulePacks = modulePacks;
    }

    public int getFreeSpaceInCargo() {
        return freeSpaceInCargo;
    }

    public void setFreeSpaceInCargo(int freeSpaceInCargo) {
        this.freeSpaceInCargo = freeSpaceInCargo;
    }

    public int getCargoCapacity() {
        return cargoCapacity;
    }

    public void setCargoCapacity(int cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    public long getMarketBalance() {
        return marketBalance;
    }

    public void setMarketBalance(long marketBalance) {
        this.marketBalance = marketBalance;
    }

    public int getPioneerSupplyTax() {
        return pioneerSupplyTax;
    }

    public void setPioneerSupplyTax(int pioneerSupplyTax) {
        this.pioneerSupplyTax = pioneerSupplyTax;
    }

    public int getShipYardSupplyTax() {
        return shipYardSupplyTax;
    }

    public void setShipYardSupplyTax(int shipYardSupplyTax) {
        this.shipYardSupplyTax = shipYardSupplyTax;
    }

    public int getRearmSupplyTax() {
        return rearmSupplyTax;
    }

    public void setRearmSupplyTax(int rearmSupplyTax) {
        this.rearmSupplyTax = rearmSupplyTax;
    }

    public int getRefuelSupplyTax() {
        return refuelSupplyTax;
    }

    public void setRefuelSupplyTax(int refuelSupplyTax) {
        this.refuelSupplyTax = refuelSupplyTax;
    }

    public int getRepairSupplyTax() {
        return repairSupplyTax;
    }

    public void setRepairSupplyTax(int repairSupplyTax) {
        this.repairSupplyTax = repairSupplyTax;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public void setFuelLevel(int fuelLevel) {
        this.fuelSupply = fuelLevel;
    }

    public int getFuelSupply() {
        return fuelSupply;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
