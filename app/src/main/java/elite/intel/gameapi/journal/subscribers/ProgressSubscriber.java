package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.ProgressEvent;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class ProgressSubscriber {

    @Subscribe
    public void onProgressEvent(ProgressEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        RankAndProgressDto rp = playerSession.getRankAndProgressDto();
        rp.setProgressData(event);
        playerSession.setRankAndProgressDto(rp);
    }

}
