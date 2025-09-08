package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.ai.ConfigManager;
import elite.companion.gameapi.journal.events.CommanderEvent;
import elite.companion.session.PlayerSession;
import elite.companion.util.DaftSecretarySanitizer;

import static elite.companion.session.PlayerSession.PLAYER_NAME;

@SuppressWarnings("unused")
public class CommanderEventSubscriber {

    @Subscribe
    public void onEvent(CommanderEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        ConfigManager configManager = ConfigManager.getInstance();

        String inGameName = event.getName();
        String alternativeName = configManager.getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);

        session.put(PLAYER_NAME, DaftSecretarySanitizer.getInstance().capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName));
    }
}
