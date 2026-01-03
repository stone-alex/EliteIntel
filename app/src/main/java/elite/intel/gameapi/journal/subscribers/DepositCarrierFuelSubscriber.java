package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.gameapi.journal.events.CarrierDepositFuelEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;

public class DepositCarrierFuelSubscriber {

    @Subscribe public void onCarrierDepositFuelEvent(CarrierDepositFuelEvent event) {
        FleetCarrierManager manager = FleetCarrierManager.getInstance();
        CarrierDataDto carrierDataDto = manager.get();
        carrierDataDto.setFuelLevel(event.getTotal());
        manager.save(carrierDataDto);
    }
}
