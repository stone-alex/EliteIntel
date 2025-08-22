package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.StatisticsEvent;
import elite.companion.session.SessionTracker;

public class StatisticsSubscriber {

    public StatisticsSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onStatisticsEvent(StatisticsEvent event) {
        StringBuilder data = new StringBuilder();

        if(event.getBankAccount() != null) {
            data.append("Current wealth: " + event.getBankAccount().getCurrentWealth() + " credits, ");
            data.append("Insurance claims: " + event.getBankAccount().getInsuranceClaims() + " credits, ");
            data.append("Number of ships owned: " + event.getBankAccount().getOwnedShipCount() + ", ");
        }

        if(event.getCombat() != null) {
            data.append("Total bounty claimed: " + event.getCombat().getBountiesClaimed() + ", ");
            data.append("Total bounty profit: " + event.getCombat().getBountyHuntingProfit() + " credits, ");
        }

        if(event.getExploration() != null) {
            data.append("Total distance traveled: " + event.getExploration().getGreatestDistanceFromStart() + ", ");
            data.append("Total systems visited: " + event.getExploration().getSystemsVisited() + ", ");
            data.append("Total hyperspace distance: " + event.getExploration().getTotalHyperspaceDistance() + ", ");
            data.append("Profits from exploration: " + event.getExploration().getExplorationProfits() + " credits, ");
        }

        if(event.getExobiology() != null) {
            data.append("Spicies First logged: " + event.getExobiology().getFirstLogged() + ", ");
            data.append("total profit: " + event.getExobiology().getOrganicDataProfits() + " credits, ");
        }


        if(event.getTrading() != null) {
            data.append("total goods sold: " + event.getTrading().getGoodsSold() + ", ");
            data.append("highest single transaction: " + event.getTrading().getHighestSingleTransaction() + " credits, ");
            data.append("market profits: " + event.getTrading().getMarketProfits() + " credits, ");
        }

        SessionTracker.getInstance().updateSession("player_stats", data.toString());
    }
}
