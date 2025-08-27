package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.EventTracker;
import elite.companion.gameapi.journal.events.LoadGameEvent;
import elite.companion.gameapi.journal.events.userfriendly.RankDto;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import static elite.companion.session.PlayerSession.*;

public class GameStartedEventSubscriber {

    public GameStartedEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();

        playerSession.updateSession(SHIP_FUEL_LEVEL, event.getFuelLevel());
        playerSession.updateSession(PLAYER_NAME, /*event.getCommander()*/"Krondor");
        playerSession.updateSession(CURRENT_SHIP, event.getShip());
        playerSession.updateSession(CURRENT_SHIP_NAME, event.getShipName());
        playerSession.updateSession(PERSONAL_CREDITS_AVAILABLE, event.getCredits());

        RankDto ranks= (RankDto) systemSession.getObject(SystemSession.RANK);
        GameLoadedInfo info = new GameLoadedInfo(event.toJson(), ranks.toJson());

        SystemSession.getInstance().setSensorData("New Game started (debugging session) " + info.toJson());
    }

    private class GameLoadedInfo {
        String loadGameEventData;
        String ranks;

        public GameLoadedInfo(String loadGameEventData, String ranks) {
            this.loadGameEventData = loadGameEventData;
            this.ranks = ranks;
        }

        public String getLoadGameEventData() {
            return loadGameEventData;
        }

        public String getRanks() {
            return ranks;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
