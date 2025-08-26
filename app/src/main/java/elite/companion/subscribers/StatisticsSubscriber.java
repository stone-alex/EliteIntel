package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.StatisticsEvent;
import elite.companion.session.PlayerSession;

import static elite.companion.session.PlayerSession.*;

public class StatisticsSubscriber {

    public StatisticsSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onStatisticsEvent(StatisticsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        if (event.getBankAccount() != null) {
            playerSession.updateSession(PERSONAL_CREDITS_AVAILABLE, event.getBankAccount().getCurrentWealth());
            playerSession.updateSession(INSURANCE_CLAIMS, event.getBankAccount().getInsuranceClaims());
            playerSession.updateSession(SHIPS_OWNED, event.getBankAccount().getOwnedShipCount());
        }

        if (event.getCombat() != null) {
            playerSession.updateSession(TOTAL_BOUNTY_CLAIMED, event.getCombat().getBountiesClaimed());
            playerSession.updateSession(TOTAL_BOUNTY_PROFIT, event.getCombat().getBountyHuntingProfit());
        }

        if (event.getExploration() != null) {
            playerSession.updateSession(TOTAL_DISTANCE_TRAVELED, event.getExploration().getGreatestDistanceFromStart());
            playerSession.updateSession(TOTAL_SYSTEMS_VISITED, event.getExploration().getSystemsVisited());
            playerSession.updateSession(TOTAL_HIPERSPACE_DISTANCE, event.getExploration().getTotalHyperspaceDistance());
            playerSession.updateSession(TOTAL_PROFITS_FROM_EXPLORATION, event.getExploration().getExplorationProfits());
        }

        if (event.getExobiology() != null) {
            playerSession.updateSession(SPECIES_FIRST_LOGGED, event.getExobiology().getFirstLogged());
            playerSession.updateSession(EXOBIOLOGY_PROFITS, event.getExobiology().getOrganicDataProfits());
        }

        if (event.getTrading() != null) {
            playerSession.updateSession(GOODS_SOLD_THIS_SESSION, event.getTrading().getGoodsSold());
            playerSession.updateSession(HIGHEST_SINGLE_TRANSACTION, event.getTrading().getHighestSingleTransaction());
            playerSession.updateSession(MARKET_PROFITS, event.getTrading().getMarketProfits());
        }

        if (event.getCrew() != null) {
            if (event.getCrew().getNpcCrewTotalWages() > 1) {
                playerSession.updateSession(CREW_WAGS_PAYOUT, event.getCrew().getNpcCrewTotalWages());
            }
        }
    }
}
