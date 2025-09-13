package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.journal.events.CommanderEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.DaftSecretarySanitizer;

import static elite.intel.session.PlayerSession.PLAYER_NAME;

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
