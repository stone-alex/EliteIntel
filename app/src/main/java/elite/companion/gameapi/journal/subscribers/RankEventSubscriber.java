package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.RankEvent;
import elite.companion.gameapi.journal.events.dto.RankAndProgressDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.ConfigManager;

@SuppressWarnings("unused")
public class RankEventSubscriber {

    @Subscribe
    public void onRankEvent(RankEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        RankAndProgressDto rp = playerSession.getRankAndProgressDto();
        rp.setRanksData(event);
        playerSession.setRankAndProgressDto(rp);

        playerSession.put(PlayerSession.PLAYER_HIGHEST_MILITARY_RANK, rp.getHighestMilitaryRank());
        String title = ConfigManager.getInstance().readUserConfig().get("title");
        playerSession.put(PlayerSession.PLAYER_TITLE, title);
    }
}
