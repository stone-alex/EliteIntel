package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.StatisticsEvent;
import elite.companion.session.PlayerSession;

import static elite.companion.session.PlayerSession.*;

@SuppressWarnings("unused")
public class StatisticsSubscriber {

    @Subscribe
    public void onStatisticsEvent(StatisticsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        if (event.getBankAccount() != null) {
            playerSession.put(PERSONAL_CREDITS_AVAILABLE, event.getBankAccount().getCurrentWealth());
            playerSession.put(INSURANCE_CLAIMS, event.getBankAccount().getInsuranceClaims());
            playerSession.put(SHIPS_OWNED, event.getBankAccount().getOwnedShipCount());
        }

        if (event.getCombat() != null) {
            playerSession.put(TOTAL_BOUNTY_CLAIMED, event.getCombat().getBountiesClaimed());
            playerSession.put(TOTAL_BOUNTY_PROFIT, event.getCombat().getBountyHuntingProfit());
        }

        if (event.getExploration() != null) {
            playerSession.put(TOTAL_DISTANCE_TRAVELED, event.getExploration().getGreatestDistanceFromStart());
            playerSession.put(TOTAL_SYSTEMS_VISITED, event.getExploration().getSystemsVisited());
            playerSession.put(TOTAL_HIPERSPACE_DISTANCE, event.getExploration().getTotalHyperspaceDistance());
            playerSession.put(TOTAL_PROFITS_FROM_EXPLORATION, event.getExploration().getExplorationProfits());
        }

        if (event.getExobiology() != null) {
            playerSession.put(SPECIES_FIRST_LOGGED, event.getExobiology().getFirstLogged());
            playerSession.put(EXOBIOLOGY_PROFITS, event.getExobiology().getOrganicDataProfits());
        }

        if (event.getTrading() != null) {
            playerSession.put(GOODS_SOLD_THIS_SESSION, event.getTrading().getGoodsSold());
            playerSession.put(HIGHEST_SINGLE_TRANSACTION, event.getTrading().getHighestSingleTransaction());
            playerSession.put(MARKET_PROFITS, event.getTrading().getMarketProfits());
        }

        if (event.getCrew() != null) {
            if (event.getCrew().getNpcCrewTotalWages() > 1) {
                playerSession.put(CREW_WAGS_PAYOUT, event.getCrew().getNpcCrewTotalWages());
            }
        }
    }
}
