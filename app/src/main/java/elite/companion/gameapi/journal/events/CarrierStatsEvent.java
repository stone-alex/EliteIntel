package elite.companion.gameapi.journal.events;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class CarrierStatsEvent extends BaseEvent {
    private long CarrierID;
    private String CarrierType;
    private String Callsign;
    private String Name;
    private String DockingAccess;
    private boolean AllowNotorious;
    private int FuelLevel;
    private double JumpRangeCurr;
    private double JumpRangeMax;
    private boolean PendingDecommission;
    private SpaceUsage SpaceUsage;
    private Finance Finance;
    private List<Crew> Crew;
    private List<Object> ShipPacks;
    private List<Object> ModulePacks;

    public static class SpaceUsage {
        private int TotalCapacity;
        private int Crew;
        private int Cargo;
        private int CargoSpaceReserved;
        private int ShipPacks;
        private int ModulePacks;
        private int FreeSpace;

        public int getTotalCapacity() {
            return TotalCapacity;
        }

        public void setTotalCapacity(int totalCapacity) {
            this.TotalCapacity = totalCapacity;
        }

        public int getCrew() {
            return Crew;
        }

        public void setCrew(int crew) {
            this.Crew = crew;
        }

        public int getCargo() {
            return Cargo;
        }

        public void setCargo(int cargo) {
            this.Cargo = cargo;
        }

        public int getCargoSpaceReserved() {
            return CargoSpaceReserved;
        }

        public void setCargoSpaceReserved(int cargoSpaceReserved) {
            this.CargoSpaceReserved = cargoSpaceReserved;
        }

        public int getShipPacks() {
            return ShipPacks;
        }

        public void setShipPacks(int shipPacks) {
            this.ShipPacks = shipPacks;
        }

        public int getModulePacks() {
            return ModulePacks;
        }

        public void setModulePacks(int modulePacks) {
            this.ModulePacks = modulePacks;
        }

        public int getFreeSpace() {
            return FreeSpace;
        }

        public void setFreeSpace(int freeSpace) {
            this.FreeSpace = freeSpace;
        }
    }

    public static class Finance {
        private long CarrierBalance;
        private long ReserveBalance;
        private long AvailableBalance;
        private int TaxRate_pioneersupplies;
        private int TaxRate_shipyard;
        private int TaxRate_rearm;
        private int TaxRate_refuel;
        private int TaxRate_repair;

        public long getCarrierBalance() {
            return CarrierBalance;
        }

        public void setCarrierBalance(long carrierBalance) {
            this.CarrierBalance = carrierBalance;
        }

        public long getReserveBalance() {
            return ReserveBalance;
        }

        public void setReserveBalance(long reserveBalance) {
            this.ReserveBalance = reserveBalance;
        }

        public long getAvailableBalance() {
            return AvailableBalance;
        }

        public void setAvailableBalance(long availableBalance) {
            this.AvailableBalance = availableBalance;
        }

        public int getTaxRate_pioneersupplies() {
            return TaxRate_pioneersupplies;
        }

        public void setTaxRate_pioneersupplies(int taxRate) {
            this.TaxRate_pioneersupplies = taxRate;
        }

        public int getTaxRate_shipyard() {
            return TaxRate_shipyard;
        }

        public void setTaxRate_shipyard(int taxRate) {
            this.TaxRate_shipyard = taxRate;
        }

        public int getTaxRate_rearm() {
            return TaxRate_rearm;
        }

        public void setTaxRate_rearm(int taxRate) {
            this.TaxRate_rearm = taxRate;
        }

        public int getTaxRate_refuel() {
            return TaxRate_refuel;
        }

        public void setTaxRate_refuel(int taxRate) {
            this.TaxRate_refuel = taxRate;
        }

        public int getTaxRate_repair() {
            return TaxRate_repair;
        }

        public void setTaxRate_repair(int taxRate) {
            this.TaxRate_repair = taxRate;
        }
    }

    public static class Crew {
        private String CrewRole;
        private boolean Activated;
        private boolean Enabled;
        private String CrewName;

        public String getCrewRole() {
            return CrewRole;
        }

        public void setCrewRole(String crewRole) {
            this.CrewRole = crewRole;
        }

        public boolean isActivated() {
            return Activated;
        }

        public void setActivated(boolean activated) {
            this.Activated = activated;
        }

        public boolean isEnabled() {
            return Enabled;
        }

        public void setEnabled(boolean enabled) {
            this.Enabled = enabled;
        }

        public String getCrewName() {
            return CrewName;
        }

        public void setCrewName(String crewName) {
            this.CrewName = crewName;
        }
    }

    public CarrierStatsEvent(String timestamp) {
        //super(timestamp, 1, Duration.ofSeconds(10));
        super(String.valueOf(Instant.now()), 1, Duration.ofSeconds(10), CarrierStatsEvent.class.getName());
    }

    public long getCarrierID() {
        return CarrierID;
    }

    public void setCarrierID(long carrierID) {
        this.CarrierID = carrierID;
    }

    public String getCarrierType() {
        return CarrierType;
    }

    public void setCarrierType(String carrierType) {
        this.CarrierType = carrierType;
    }

    public String getCallsign() {
        return Callsign;
    }

    public void setCallsign(String callsign) {
        this.Callsign = callsign;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getDockingAccess() {
        return DockingAccess;
    }

    public void setDockingAccess(String dockingAccess) {
        this.DockingAccess = dockingAccess;
    }

    public boolean isAllowNotorious() {
        return AllowNotorious;
    }

    public void setAllowNotorious(boolean allowNotorious) {
        this.AllowNotorious = allowNotorious;
    }

    public int getFuelLevel() {
        return FuelLevel;
    }

    public void setFuelLevel(int fuelLevel) {
        this.FuelLevel = fuelLevel;
    }

    public double getJumpRangeCurr() {
        return JumpRangeCurr;
    }

    public void setJumpRangeCurr(double jumpRangeCurr) {
        this.JumpRangeCurr = jumpRangeCurr;
    }

    public double getJumpRangeMax() {
        return JumpRangeMax;
    }

    public void setJumpRangeMax(double jumpRangeMax) {
        this.JumpRangeMax = jumpRangeMax;
    }

    public boolean isPendingDecommission() {
        return PendingDecommission;
    }

    public void setPendingDecommission(boolean pendingDecommission) {
        this.PendingDecommission = pendingDecommission;
    }

    public SpaceUsage getSpaceUsage() {
        return SpaceUsage;
    }

    public void setSpaceUsage(SpaceUsage spaceUsage) {
        this.SpaceUsage = spaceUsage;
    }

    public Finance getFinance() {
        return Finance;
    }

    public void setFinance(Finance finance) {
        this.Finance = finance;
    }

    public List<Crew> getCrew() {
        return Crew;
    }

    public void setCrew(List<Crew> crew) {
        this.Crew = crew;
    }

    public List<Object> getShipPacks() {
        return ShipPacks;
    }

    public void setShipPacks(List<Object> shipPacks) {
        this.ShipPacks = shipPacks;
    }

    public List<Object> getModulePacks() {
        return ModulePacks;
    }

    public void setModulePacks(List<Object> modulePacks) {
        this.ModulePacks = modulePacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrierStatsEvent that = (CarrierStatsEvent) o;
        return CarrierID == that.CarrierID &&
                AllowNotorious == that.AllowNotorious &&
                FuelLevel == that.FuelLevel &&
                Double.compare(that.JumpRangeCurr, JumpRangeCurr) == 0 &&
                Double.compare(that.JumpRangeMax, JumpRangeMax) == 0 &&
                PendingDecommission == that.PendingDecommission &&
                Objects.equals(CarrierType, that.CarrierType) &&
                Objects.equals(Callsign, that.Callsign) &&
                Objects.equals(Name, that.Name) &&
                Objects.equals(DockingAccess, that.DockingAccess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CarrierID, CarrierType, Callsign, Name, DockingAccess,
                AllowNotorious, FuelLevel, JumpRangeCurr, JumpRangeMax, PendingDecommission);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CarrierStatsEvent.class.getSimpleName() + "[", "]")
                .add("CarrierID=" + CarrierID)
                .add("CarrierType='" + CarrierType + "'")
                .add("Callsign='" + Callsign + "'")
                .add("Name='" + Name + "'")
                .add("DockingAccess='" + DockingAccess + "'")
                .add("AllowNotorious=" + AllowNotorious)
                .add("FuelLevel=" + FuelLevel)
                .add("JumpRangeCurr=" + JumpRangeCurr)
                .add("JumpRangeMax=" + JumpRangeMax)
                .add("PendingDecommission=" + PendingDecommission)
                .toString();
    }
}
