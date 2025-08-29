package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ProgressEvent;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class ProgressSubscriber {

    @Subscribe
    public void onProgressEvent(ProgressEvent event) {
        setData(event.getCombat(), event.getExobiologist(), event.getExplore(), event.getEmpire(), event.getFederation(), event.getSoldier(), event);
    }

    static void setData(int combat, int exobiologist, int explore, int empire, int federation, int soldier, ProgressEvent event) {
        RankDto rankDto = new RankDto();
        rankDto.setData(event);
        SystemSession.getInstance().updateSession(SystemSession.RANK, rankDto);
    }
}
