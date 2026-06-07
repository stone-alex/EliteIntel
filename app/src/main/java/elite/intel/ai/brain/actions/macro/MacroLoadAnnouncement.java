package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.i18n.MultiLingualTextProvider;
import elite.intel.util.AppPaths;
import elite.intel.util.StringUtls;

/**
 * Announces macro load results via TTS on application startup.
 * Call {@link #announce()} after {@link MacroRegistry#load()} has completed and
 * TTS subscribers are active.
 */
public class MacroLoadAnnouncement {

    private static volatile MacroLoadAnnouncement instance;

    private MacroLoadAnnouncement() {
    }

    public static synchronized MacroLoadAnnouncement getInstance() {
        if (instance == null) instance = new MacroLoadAnnouncement();
        return instance;
    }

    /**
     * Speaks a localized summary of the macro load result.
     * Reports the number of successfully loaded macros and, separately,
     * the number of macros skipped due to validation failures.
     */
    public void announce() {
        int loaded = MacroRegistry.getInstance().getMacros().size();
        int skipped = MacroRegistry.getInstance().getSkippedOnLastLoad();
        String playerName = PlayerSession.getInstance().getVariablePlayerName();

        if (loaded > 0 && skipped > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosLoadedWithSkipped", playerName, loaded, skipped)
            ));
        } else if (loaded > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosLoaded", playerName, loaded)
            ));
        } else if (skipped > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosSkipped", playerName, skipped)
            ));
        }

        if (skipped > 0) {
            MacroRegistry.getInstance().getSkippedLabelsOnLastLoad()
                    .forEach(label -> EventBusManager.publish(new AppLogEvent(
                            MultiLingualTextProvider.getText("actions.macros.load.invalid", label))));
        }

        if (MacroRegistry.getInstance().wasLastLoadRestoredFromBackup()) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosRestoredFromBackup", playerName)
            ));
            EventBusManager.publish(new AppLogEvent(
                    MultiLingualTextProvider.getText(
                            "actions.macros.load.corruptRestoredFromBackup",
                            AppPaths.CUSTOM_COMMANDS_FILE_NAME)));
        }
    }
}
