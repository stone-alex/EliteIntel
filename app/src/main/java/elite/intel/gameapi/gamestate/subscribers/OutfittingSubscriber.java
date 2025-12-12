package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.eddn.EdDnClient;
import elite.intel.eddn.ZMQUtil;
import elite.intel.eddn.mappers.OutfittingMapper;
import elite.intel.eddn.schemas.EddnHeader;
import elite.intel.eddn.schemas.EddnPayload;
import elite.intel.eddn.schemas.OutfittingMessage;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class OutfittingSubscriber {

    @Subscribe public void onOutfittingEvent(GameEvents.OutfittingEvent event) {

        SystemSession systemSession = SystemSession.getInstance();
        if (!systemSession.isSendOutfittingData()) return;



        PlayerSession playerSession = PlayerSession.getInstance();
        EddnHeader header = new EddnHeader(ZMQUtil.generateUploaderID());
        header.setGameVersion(playerSession.getGameVersion());
        header.setGameBuild(playerSession.getGameBuild());

        OutfittingMessage message = OutfittingMapper.map(event);
        EddnPayload<OutfittingMessage> payload = new EddnPayload<>(
                "https://eddn.edcd.io/schemas/outfitting/2",
                header,
                message
        );

        if(event.getItems().size() > 0) {
            EdDnClient.getInstance().upload(payload);
        }

    }
}
