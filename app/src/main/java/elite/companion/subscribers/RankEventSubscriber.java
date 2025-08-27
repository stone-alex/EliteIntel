package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.RankEvent;
import elite.companion.util.Ranks;

public class RankEventSubscriber {

    public RankEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onRankEvent(RankEvent event) {
        String combatRank = Ranks.getCombatRankMap().get(event.getCombat());
        String militaryRankEmpire = Ranks.getImperialRankMap().get(event.getEmpire());
        String militaryRankFederation = Ranks.getFederationRankMap().get(event.getFederation());
        String highestMilitaryRank = Ranks.getHighestRankAsString(event.getEmpire(), event.getFederation());
        String honorific = Ranks.getHonorific(event.getEmpire(), event.getFederation());
        String exobiologyRank = Ranks.getExobiologyRankMap().get(event.getExobiologist());
        String explorationRank = Ranks.getExplorationRankMap().get(event.getExplore());
        String mersenaryRank = Ranks.getMercenaryRankMap().get(event.getSoldier());
    }
}
