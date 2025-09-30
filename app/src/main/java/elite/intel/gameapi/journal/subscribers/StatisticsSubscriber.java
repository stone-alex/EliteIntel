package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.StatisticsEvent;
import elite.intel.session.PlayerSession;

import static elite.intel.session.PlayerSession.*;

@SuppressWarnings("unused")
public class StatisticsSubscriber {

    @Subscribe
    public void onStatisticsEvent(StatisticsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        if (event.getBankAccount() != null) {
            playerSession.setPersonalCreditsAvailable(event.getBankAccount().getCurrentWealth());
            playerSession.setInsuranceClaims(event.getBankAccount().getInsuranceClaims());
            playerSession.setShipsOwned(event.getBankAccount().getOwnedShipCount());
        }

        if (event.getCombat() != null) {
            playerSession.setTotalBountyClaimed(event.getCombat().getBountiesClaimed());
            playerSession.setTotalBountyProfit(event.getCombat().getBountyHuntingProfit());
        }

        if (event.getExploration() != null) {
            playerSession.setTotalDistanceTraveled(event.getExploration().getGreatestDistanceFromStart());
            playerSession.setTotalSystemsVisited(event.getExploration().getSystemsVisited());
            playerSession.setTotalHyperspaceDistance(event.getExploration().getTotalHyperspaceDistance());
            playerSession.setTotalProfitsFromExploration(event.getExploration().getExplorationProfits());
        }

        if (event.getExobiology() != null) {
            playerSession.setSpeciesFirstLogged(event.getExobiology().getFirstLogged());
            playerSession.setExobiologyProfits(event.getExobiology().getOrganicDataProfits());
        }

        if (event.getTrading() != null) {
            playerSession.setGoodsSoldThisSession(event.getTrading().getGoodsSold());
            playerSession.setHighestSingleTransaction(event.getTrading().getHighestSingleTransaction());
            playerSession.setMarketProfits(event.getTrading().getMarketProfits());
        }

        if (event.getCrew() != null) {
            if (event.getCrew().getNpcCrewTotalWages() > 1) {
                playerSession.setCrewWagsPayout(event.getCrew().getNpcCrewTotalWages());
            }
        }
    }
}
