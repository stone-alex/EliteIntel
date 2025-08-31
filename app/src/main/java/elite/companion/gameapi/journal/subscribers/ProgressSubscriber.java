package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ProgressEvent;
import elite.companion.gameapi.journal.events.dto.RankAndProgressDto;
import elite.companion.session.PlayerSession;

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
