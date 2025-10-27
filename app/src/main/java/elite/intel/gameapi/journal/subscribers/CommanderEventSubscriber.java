package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.journal.events.CommanderEvent;
import elite.intel.session.PlayerSession;

import static elite.intel.util.StringUtls.capitalizeWords;

@SuppressWarnings("unused")
public class CommanderEventSubscriber {

    @Subscribe
    public void onEvent(CommanderEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        ConfigManager configManager = ConfigManager.getInstance();

        String inGameName = event.getName();
        String alternativeName = configManager.getPlayerKey(ConfigManager.PLAYER_ALTERNATIVE_NAME);

        session.setPlayerName(capitalizeWords(alternativeName != null || !alternativeName.isEmpty() ? alternativeName : inGameName));
    }
}
