package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.RankEvent;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class RankEventSubscriber {

    @Subscribe
    public void onRankEvent(RankEvent event) {
        RankDto rankDto = new RankDto();
        rankDto.setData(event);
        SystemSession.getInstance().put(SystemSession.RANK, rankDto);
    }
}
