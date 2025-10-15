package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.CargoTransferEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;

import java.util.List;

public class CargoTransferSubscriber {

    private PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onCargoTransfer(CargoTransferEvent event) {

        List<CargoTransferEvent.Transfer> transfers = event.getTransfers();
        CarrierDataDto carrierData = playerSession.getCarrierData();

        for(CargoTransferEvent.Transfer transfer : transfers) {
            String commodity = transfer.getType();
            int amount = transfer.getCount();

            if("tocarrier".equalsIgnoreCase(transfer.getDirection())) {
                carrierData.addCommodity(commodity, amount);
            }

            if("toship".equalsIgnoreCase(transfer.getDirection())) {
                carrierData.removeCommodity(commodity, amount);
            }
        }
        playerSession.setCarrierData(carrierData);
    }
}
