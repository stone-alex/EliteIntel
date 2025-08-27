package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.RankEvent;
import elite.companion.gameapi.journal.events.userfriendly.RankDto;
import elite.companion.session.SystemSession;
import elite.companion.util.Ranks;

public class RankEventSubscriber {

    public RankEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onRankEvent(RankEvent event) {
        RankDto rankDto = new RankDto();
        rankDto.setCombatRank(Ranks.getCombatRankMap().get(event.getCombat()));
        rankDto.setExobiologyRank(Ranks.getExobiologyRankMap().get(event.getExobiologist()));
        rankDto.setExplorationRank(Ranks.getExplorationRankMap().get(event.getExplore()));
        rankDto.setHighestMilitaryRank(Ranks.getHighestRankAsString(event.getEmpire(), event.getFederation()));
        rankDto.setMilitaryRankEmpire(Ranks.getImperialRankMap().get(event.getEmpire()));
        rankDto.setMilitaryRankFederation(Ranks.getFederationRankMap().get(event.getFederation()));
        rankDto.setMercenaryRank(Ranks.getMercenaryRankMap().get(event.getSoldier()));
        rankDto.setHonorific(Ranks.getHonorific(event.getEmpire(), event.getFederation()));
        SystemSession.getInstance().updateSession(SystemSession.RANK, rankDto);
    }
}
