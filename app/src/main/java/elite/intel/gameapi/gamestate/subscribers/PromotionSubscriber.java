package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.PromotionEvent;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.Ranks;

public class PromotionSubscriber {

    @Subscribe
    public void onPromotionEvent(PromotionEvent event) {
        Integer imperialNavyRank = event.getEmpire();
        Integer combatRank = event.getCombat();
        Integer tradeRank = event.getTrade();
        Integer exploreRank = event.getExplore();
        Integer soldier = event.getSoldier();
        Integer federalNavyRank = event.getFederation();
        Integer exobiologyRank = event.getExobiologist();

        PlayerSession session = PlayerSession.getInstance();
        RankAndProgressDto rankAndProgressDto = session.getRankAndProgressDto();

        if (imperialNavyRank != null) {
            String string = Ranks.getImperialRankMap().get(imperialNavyRank);
            EventBusManager.publish(new SensorDataEvent("Imperial Navy promotion to Rank: " + string));
            rankAndProgressDto.setMilitaryRankEmpire(string);
            session.setRankAndProgressDto(rankAndProgressDto);
        }
        if (federalNavyRank != null) {
            String string = Ranks.getFederationRankMap().get(federalNavyRank);
            EventBusManager.publish(new SensorDataEvent("Federation Navy promotion to Rank: " + string));
            rankAndProgressDto.setMilitaryRankFederation(string);
            session.setRankAndProgressDto(rankAndProgressDto);
        }
        if (combatRank != null) {
            String string = Ranks.getCombatRankMap().get(combatRank);
            EventBusManager.publish(new SensorDataEvent("Pilate Federation promotion to combat rank of: " + string));
            rankAndProgressDto.setCombatRank(string);
            session.setRankAndProgressDto(rankAndProgressDto);
        }

        //other ranks are not used for honorifics etc. the data will update on next game session load.
        if (tradeRank != null) {
            String string = Ranks.getTradeRankMap().get(tradeRank);
            EventBusManager.publish(new SensorDataEvent("Pilate Federation promotion to trade rank of: " + string));
        }
        if (exploreRank != null) {
            String string = Ranks.getExplorationRankMap().get(exploreRank);
            EventBusManager.publish(new SensorDataEvent("Pilate Federation promotion to exploration rank of: " + string));
        }
        if (exobiologyRank != null) {
            String string = Ranks.getExobiologyRankMap().get(exobiologyRank);
            EventBusManager.publish(new SensorDataEvent("Pilate Federation promotion to exobiology rank of: " + string));
        }
        if (soldier != null) {
            String string = Ranks.getMercenaryRankMap().get(soldier);
            EventBusManager.publish(new SensorDataEvent("Pilate Federation promotion to soldier count of: " + string));
        }
    }
}
