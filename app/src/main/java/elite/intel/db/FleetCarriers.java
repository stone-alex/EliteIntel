package elite.intel.db;

import elite.intel.db.dao.FleetCarrierDao;
import elite.intel.db.dao.FleetCarrierDao.FleetCarrier;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.CarrierStatsEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.util.json.GsonFactory;

public class FleetCarriers {

    private static FleetCarriers instance;

    private FleetCarriers() {
    }

    public static synchronized FleetCarriers getInstance() {
        if (instance == null) {
            instance = new FleetCarriers();
        }
        return instance;
    }

    public void save(CarrierDataDto data) {
        Database.withDao(FleetCarrierDao.class, dao -> {
            FleetCarrier carrier = new FleetCarrier();
            carrier.setJson(data.toJson());
            dao.save(carrier);
            return null;
        });

    }

    public CarrierDataDto get() {
        return Database.withDao(FleetCarrierDao.class, dao -> {
            FleetCarrier fleetCarrier = dao.get();
            if(fleetCarrier == null) return new CarrierDataDto();
            return GsonFactory.getGson().fromJson(fleetCarrier.getJson(), CarrierDataDto.class);
        });
    }


    public void setCarrierStats(CarrierStatsEvent event) {
        CarrierStatsEvent.Finance finance = event.getFinance();
        CarrierDataDto carrierData = get();
        carrierData.setCallSign(event.getCallsign());
        carrierData.setCarrierName(event.getName());
        carrierData.setCarrierType(event.getCarrierType());
        carrierData.setDockingAccess(event.getDockingAccess());
        carrierData.setCurrentJumpRange(event.getJumpRangeCurr());
        carrierData.setMaxJumpRange(event.getJumpRangeMax());
        carrierData.setAllowNotorious(event.isAllowNotorious());
        carrierData.setPendingDecommission(event.isPendingDecommission());
        carrierData.setFuelLevel(event.getFuelLevel());

        if (event.getSpaceUsage() != null) {
            CarrierStatsEvent.SpaceUsage spaceUsage = event.getSpaceUsage();
            carrierData.setCargoSpaceUsed(spaceUsage.getCargo());
            carrierData.setCargoSpaceReserved(spaceUsage.getCargoSpaceReserved());
            carrierData.setShipRacks(spaceUsage.getShipPacks());
            carrierData.setModulePacks(spaceUsage.getModulePacks());
            carrierData.setFreeSpaceInCargo(spaceUsage.getFreeSpace());
            carrierData.setCargoCapacity(spaceUsage.getTotalCapacity());
        }

        if (finance != null) {
            carrierData.setTotalBalance(finance.getCarrierBalance());
            carrierData.setReserveBalance(finance.getReserveBalance());
            carrierData.setMarketBalance(finance.getAvailableBalance());
            carrierData.setPioneerSupplyTax(finance.getTaxRate_pioneersupplies());
            carrierData.setShipYardSupplyTax(finance.getTaxRate_shipyard());
            carrierData.setRearmSupplyTax(finance.getTaxRate_rearm());
            carrierData.setRepairSupplyTax(finance.getTaxRate_repair());
            carrierData.setRefuelSupplyTax(finance.getTaxRate_refuel());
        }
        save(carrierData);
    }
}
