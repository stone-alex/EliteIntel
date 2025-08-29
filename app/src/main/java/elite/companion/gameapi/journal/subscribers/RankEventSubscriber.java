package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.RankEvent;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.ConfigManager;

@SuppressWarnings("unused")
public class RankEventSubscriber {

    @Subscribe
    public void onRankEvent(RankEvent event) {
        RankDto rankDto = new RankDto();
        rankDto.setData(event);
        //Player Name is read from CommanderEvent. This event does not contain it.
        PlayerSession.getInstance().put(PlayerSession.PLAYER_RANK, rankDto.getHighestMilitaryRank());

        String title = ConfigManager.getInstance().readUserConfig().get("title");
        PlayerSession.getInstance().put(PlayerSession.PLAYER_TITLE, title);
        SystemSession.getInstance().put(SystemSession.RANK, rankDto);
    }
}
