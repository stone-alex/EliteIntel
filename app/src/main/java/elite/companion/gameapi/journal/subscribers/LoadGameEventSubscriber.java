package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.LoadGameEvent;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.ConfigManager;
import elite.companion.util.GsonFactory;
import elite.companion.util.StringSanitizer;

import static elite.companion.session.PlayerSession.*;

public class LoadGameEventSubscriber {

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        SystemSession systemSession = SystemSession.getInstance();

        playerSession.put(SHIP_FUEL_LEVEL, event.getFuelLevel());

        String inGameName = event.getCommander();
        String alternativeName = ConfigManager.getInstance().readUserConfig().get(ConfigManager.PLAYER_ALTERNATIVE_NAME);
        String usePlayerName = StringSanitizer.capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName);
        playerSession.put(PLAYER_NAME, usePlayerName);

        playerSession.put(CURRENT_SHIP, event.getShip());
        playerSession.put(CURRENT_SHIP_NAME, event.getShipName());
        playerSession.put(PERSONAL_CREDITS_AVAILABLE, event.getCredits());

        RankDto ranks= (RankDto) systemSession.get(SystemSession.RANK);
        GameLoadedInfo info = new GameLoadedInfo(event.toJson(), ranks.toJson());

        SystemSession.getInstance().sendToAiAnalysis("Connected to Elite Dangerous as " + usePlayerName + ". " + ranks.toJson());

        initValuesFromConfig(playerSession);
    }

    private static void initValuesFromConfig(PlayerSession playerSession) {
        ConfigManager configManager = ConfigManager.getInstance();
        String mission_statement = configManager.readUserConfig().get("mission_statement");
        playerSession.put(PLAYER_MISSION_STATEMENT, mission_statement);
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
            return  GsonFactory.getGson().toJson(this);
        }
    }
}
