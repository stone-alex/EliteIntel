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
        PlayerSession.getInstance().put(rankDto.getHighestMilitaryRank(), PlayerSession.PLAYER_RANK);

        String title = ConfigManager.getInstance().readUserConfig().get("title");
        PlayerSession.getInstance().put(title, PlayerSession.PLAYER_TITLE);
        SystemSession.getInstance().put(SystemSession.RANK, rankDto);
    }
}
