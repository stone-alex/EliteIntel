package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.search.eddn.EdDnClient;
import elite.intel.search.eddn.ZMQUtil;
import elite.intel.search.eddn.mappers.ShipyardMapper;
import elite.intel.search.eddn.schemas.EddnHeader;
import elite.intel.search.eddn.schemas.EddnPayload;
import elite.intel.search.eddn.schemas.ShipyardMessage;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class ShipyardSubscriber {

    @Subscribe public void onShipyardEvent(GameEvents.ShipyardEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        if (!systemSession.isSendShipyardData()) return;

        PlayerSession playerSession = PlayerSession.getInstance();
        EddnHeader header = new EddnHeader(ZMQUtil.generateUploaderID());
        header.setGameVersion(playerSession.getGameVersion());
        header.setGameBuild(playerSession.getGameBuild());
        header.setSoftwareVersion(systemSession.readVersionFromResources());

        ShipyardMessage message = ShipyardMapper.map(event);
        EddnPayload<ShipyardMessage> payload = new EddnPayload<>(
                "https://eddn.edcd.io/schemas/shipyard/2",
                header,
                message
        );
        if (!event.getPriceList().isEmpty()) {
            EdDnClient.getInstance().upload(payload);
        }
    }
}
