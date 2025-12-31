package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.search.eddn.EdDnClient;
import elite.intel.search.eddn.ZMQUtil;
import elite.intel.search.eddn.mappers.MarketMapper;
import elite.intel.search.eddn.schemas.CommodityMessage;
import elite.intel.search.eddn.schemas.EddnHeader;
import elite.intel.search.eddn.schemas.EddnPayload;
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
        header.setSoftwareVersion(systemSession.readVersionFromResources());

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
