package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.journal.events.RankEvent;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class RankEventSubscriber {

    @Subscribe
    public void onRankEvent(RankEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        RankAndProgressDto rp = playerSession.getRankAndProgressDto();
        rp.setRanksData(event);
        playerSession.setRankAndProgressDto(rp);

        playerSession.setPlayerHighestMilitaryRank(rp.getHighestMilitaryRank());
        String title = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_CUSTOM_TITLE);
        playerSession.setPlayerTitle(title);

    }
}
