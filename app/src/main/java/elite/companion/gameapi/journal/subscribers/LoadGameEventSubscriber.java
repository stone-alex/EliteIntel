package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.ConfigManager;
import elite.companion.gameapi.journal.events.LoadGameEvent;
import elite.companion.session.PlayerSession;
import elite.companion.util.DaftSecretarySanitizer;

import static elite.companion.session.PlayerSession.*;

public class LoadGameEventSubscriber {

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        playerSession.put(SHIP_FUEL_LEVEL, event.getFuelLevel());

        String inGameName = event.getCommander();
        String alternativeName = ConfigManager.getInstance().getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);
        String usePlayerName = DaftSecretarySanitizer.getInstance().capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName);
        playerSession.put(PLAYER_NAME, usePlayerName);

        playerSession.put(CURRENT_SHIP, event.getShip());
        playerSession.put(CURRENT_SHIP_NAME, event.getShipName());
        playerSession.put(PERSONAL_CREDITS_AVAILABLE, event.getCredits());

        initValuesFromConfig(playerSession);
    }

    private static void initValuesFromConfig(PlayerSession playerSession) {
        ConfigManager configManager = ConfigManager.getInstance();
        String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
        playerSession.put(PLAYER_MISSION_STATEMENT, mission_statement);
    }
}
