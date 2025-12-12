package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.eddn.EdDnClient;
import elite.intel.eddn.ZMQUtil;
import elite.intel.eddn.mappers.MarketMapper;
import elite.intel.eddn.schemas.CommodityMessage;
import elite.intel.eddn.schemas.EddnHeader;
import elite.intel.eddn.schemas.EddnPayload;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class MarketSubscriber {

    @Subscribe
    public void onMarketEvent(GameEvents.MarketEvent marketEvent) {
        SystemSession systemSession = SystemSession.getInstance();
        if (!systemSession.isSendMarketData()) return;


        PlayerSession session = PlayerSession.getInstance();
        session.saveMarket(marketEvent);

        CommodityMessage msg = MarketMapper.map(marketEvent);
        EddnHeader header = new EddnHeader(ZMQUtil.generateUploaderID());
        header.setGameVersion(session.getGameVersion());
        header.setGameBuild(session.getGameBuild());

        EddnPayload<CommodityMessage> payload = new EddnPayload<>(
                "https://eddn.edcd.io/schemas/commodity/3",
                header,
                msg
        );

        if (marketEvent.getItems().size() > 0) {
            EdDnClient.getInstance().upload(payload);
        }
    }
}
